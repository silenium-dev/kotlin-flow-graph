import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.toList
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

enum class DataType {
    PLAIN,
    BASE64,
    JSON,
}

class Base64Buffer : ReferenceCounted<Base64Buffer> {
    private val refCount_: AtomicLong
    val refCount: Long get() = refCount_.get()
    val buffer: ByteBuffer

    private constructor(buffer: ByteBuffer, refCount: AtomicLong) {
        println("Creating cloned buffer")
        this.buffer = buffer
        this.refCount_ = refCount
    }

    constructor(buffer: ByteBuffer) {
        this.buffer = buffer
        this.refCount_ = AtomicLong(1L)
    }

    override fun clone(): Result<Base64Buffer> {
        println("Cloning buffer")
        refCount_.incrementAndGet()
        return Result.success(Base64Buffer(buffer, refCount_))
    }

    override fun close() {
        println("Decrementing buffer ref count")
        if (refCount_.decrementAndGet() == 0L) {
            println("Closing buffer")
            buffer.clear()
        }
        println("Buffer ref count: ${refCount_.get()}")
    }
}

class Base64Decoder : SourceBase<ByteArray, DataType>(), Sink<Base64Buffer, DataType> {
    override val inputMetadata: MutableMap<UInt, DataType> =
        Collections.synchronizedMap(mutableMapOf<UInt, DataType>())
    private val queue = Channel<FlowItem<Base64Buffer, DataType>>(capacity = 4)

    @OptIn(ExperimentalEncodingApi::class)
    private val processor = CoroutineScope(Dispatchers.Default).async {
        for (item in queue) {
            delay(Random.nextInt(1..250).milliseconds)
            val array = ByteArray(item.value.buffer.remaining())
            item.value.buffer.get(array)
            item.value.close()
            publish(FlowItem(item.pad, DataType.PLAIN, Base64.decode(array)))
        }
    }

    override fun configure(pad: UInt, metadata: DataType): Result<Unit> {
        check(metadata == DataType.BASE64) { "metadata must be BASE64" }
        this.inputMetadata[pad] = metadata
        this.metadata[pad] = DataType.PLAIN
        return Result.success(Unit)
    }

    override suspend fun submit(item: FlowItem<Base64Buffer, DataType>): Result<Unit> = runCatching {
        queue.send(FlowItem(item.pad, DataType.PLAIN, item.value))
    }

    override fun close() {
        queue.close()
        runBlocking { processor.join() }
        super.close()
    }
}

class BufferSource<T, P>(vararg pads: Pair<UInt, P>): SourceBase<T, P>() {
    override val outputMetadata = pads.toMap()
    fun submit(pad: UInt, value: T) {
        runBlocking {
            publish(FlowItem(pad, outputMetadata[pad]!!, value))
        }
    }
}

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
        val listAsync = async(Dispatchers.Default) {
            decoder.flow.toList()
        }
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
        job.cancelAndJoin()
        decoder.close()
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
