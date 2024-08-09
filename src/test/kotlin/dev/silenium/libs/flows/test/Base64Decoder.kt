package dev.silenium.libs.flows.test

import dev.silenium.libs.flows.api.FlowItem
import dev.silenium.libs.flows.api.Transformer
import dev.silenium.libs.flows.base.SourceBase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

class Base64Decoder : SourceBase<ByteArray, DataType>(), Transformer<Base64Buffer, DataType, ByteArray, DataType> {
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

    override suspend fun receive(item: FlowItem<Base64Buffer, DataType>): Result<Unit> = runCatching {
        queue.send(FlowItem(item.pad, DataType.PLAIN, item.value))
    }

    override fun close() {
        queue.close()
        runBlocking { processor.join() }
        super.close()
    }
}
