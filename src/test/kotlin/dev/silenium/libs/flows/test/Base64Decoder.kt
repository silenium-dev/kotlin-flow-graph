package dev.silenium.libs.flows.test

import dev.silenium.libs.flows.api.FlowItem
import dev.silenium.libs.flows.base.JobTransformerBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalUnsignedTypes::class)
class Base64Decoder :
    JobTransformerBase<Base64Buffer, DataType, ByteArray, DataType>(CoroutineScope(Dispatchers.IO), 0u) {
    private val queue = Channel<FlowItem<Base64Buffer, DataType>>(capacity = 4)

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun CoroutineScope.run() {
        println("Base64Decoder.run")
        for (item in queue) {
            delay(Random.nextInt(1..250).milliseconds)
            val array = ByteArray(item.value.buffer.remaining())
            item.value.buffer.get(array)
            item.value.close()
            publish(FlowItem(item.pad, DataType.PLAIN, Base64.decode(array)))
        }
    }

    override fun outputMetadata(inputMetadata: DataType): DataType {
        check(inputMetadata == DataType.BASE64) { "metadata must be BASE64" }
        return DataType.PLAIN
    }

    override suspend fun receive(item: FlowItem<Base64Buffer, DataType>): Result<Unit> = runCatching {
        queue.send(FlowItem(item.pad, DataType.PLAIN, item.value))
    }

    override fun close() {
        queue.close()
        super.close()
    }
}
