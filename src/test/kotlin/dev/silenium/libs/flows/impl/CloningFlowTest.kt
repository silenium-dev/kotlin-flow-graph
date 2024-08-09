package dev.silenium.libs.flows.impl

import dev.silenium.libs.flows.api.ReferenceCounted
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class TestData(
    val buf: AtomicReference<ByteArray?>,
    private val refCount_: AtomicLong = AtomicLong(0L)
) : ReferenceCounted<TestData> {
    constructor(data: ByteArray) : this(AtomicReference(data))

    init {
        refCount_.incrementAndGet()
    }

    val refCount: Long
        get() = refCount_.get()

    override fun clone(): Result<TestData> {
        return Result.success(TestData(buf, refCount_))
    }

    override fun close() {
        if (refCount_.decrementAndGet() == 0L) {
            buf.set(null)
        }
    }
}

class CloningFlowTest : FunSpec({
    test("cloning flow clones properly") {
        val flow = CloningFlow<TestData>()

        val started = CompletableDeferred<Unit>()
        val items = async {
            started.complete(Unit)
            flow.toList()
        }
        started.await()

        val inputs = listOf(
            "Hello, World!",
            "some text",
            "Another text",
        )
        inputs.forEach { input ->
            val data = TestData(input.encodeToByteArray())
            flow.publish(data)
            data.close()
        }

        flow.close()
        items.await().map { item ->
            item.buf.get().shouldNotBeNull().decodeToString().also {
                item.close()
                item.refCount shouldBe 0L
                item.buf.get().shouldBeNull()
            }
        }.toSet() shouldBe inputs.toSet()
    }
})
