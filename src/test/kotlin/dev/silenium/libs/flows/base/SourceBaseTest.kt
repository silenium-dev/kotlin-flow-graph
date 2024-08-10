package dev.silenium.libs.flows.base

import dev.silenium.libs.flows.test.Base64Buffer
import dev.silenium.libs.flows.test.Base64Decoder
import dev.silenium.libs.flows.test.BufferSource
import dev.silenium.libs.flows.test.DataType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import java.nio.ByteBuffer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalEncodingApi::class)
class SourceBaseTest : FunSpec({
    test("SourceBase") {
        val inputs = listOf(
            "Hello, World!",
            "some text",
            "Another text",
        )

        val bufferSource = BufferSource<Base64Buffer, DataType>(0u to DataType.BASE64)
        val decoder = Base64Decoder()
        decoder.configure(0u, DataType.BASE64)
        val job = CoroutineScope(Dispatchers.Default).launch {
            bufferSource.flow.collect(decoder)
        }
        val started = CompletableDeferred<Unit>()
        val listAsync = async(Dispatchers.Default) {
            started.complete(Unit)
            decoder.flow.toList()
        }
        started.await()
        delay(100.milliseconds)
        val bufs = inputs.map { input ->
            val base64 = Base64.encode(input.encodeToByteArray())
            val byteBuffer = ByteBuffer.allocateDirect(base64.length)
            byteBuffer.put(base64.encodeToByteArray())
            byteBuffer.flip()
            val buf = Base64Buffer(byteBuffer)
            bufferSource.submit(0u, buf)
            buf.close()
            buf
        }
        decoder.close()
        bufferSource.close()
        job.join()
        listAsync.await().shouldHaveSize(inputs.size).forEach { item ->
            println(item)
            item.pad shouldBe 0u
            item.metadata shouldBe DataType.PLAIN
            item.value.decodeToString() shouldBeIn inputs
        }
        bufs.forEach {
            it.buffer.remaining() shouldBe it.buffer.capacity()
            it.buffer.position() shouldBe 0
        }
    }
})
