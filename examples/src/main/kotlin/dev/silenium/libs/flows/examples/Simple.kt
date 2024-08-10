package dev.silenium.libs.flows.examples

import dev.silenium.libs.flows.api.FlowItem
import dev.silenium.libs.flows.api.Sink
import dev.silenium.libs.flows.api.Transformer
import dev.silenium.libs.flows.base.SourceBase
import dev.silenium.libs.flows.impl.FlowGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

data class MyData(val value: Int)
data class MyMetadata(val negativeNumbers: Boolean)

// A simple source that publishes numbers from 0 to 100 to two different output pads
class MySource : SourceBase<MyData, MyMetadata>() {
    init {
        // Create two output pads, one for positive numbers and one for negative numbers
        outputMetadata_[0u] = MyMetadata(false)
        outputMetadata_[1u] = MyMetadata(true)
    }

    // Publish numbers from 0 to 100
    // Publishing can be done from any thread/coroutine, as the flow is thread-safe
    suspend fun run() {
        for (i in 0..100) {
            if (i % 2 == 0) {
                publish(FlowItem(0u, outputMetadata_[0u]!!, MyData(i)))
            } else {
                publish(FlowItem(1u, outputMetadata_[1u]!!, MyData(i)))
            }
        }
    }
}

// A simple processor that doubles the value of the input data
class MyTransformer : Transformer<MyData, MyMetadata, MyData, MyMetadata>, SourceBase<MyData, MyMetadata>() {
    private val inputMetadata_ = mutableMapOf<UInt, MyMetadata?>()
    override val inputMetadata: Map<UInt, MyMetadata?> get() = inputMetadata_

    override fun configure(pad: UInt, metadata: MyMetadata): Result<Unit> {
        inputMetadata_[pad] = metadata
        this.outputMetadata_[pad] = metadata
        return Result.success(Unit)
    }

    override suspend fun receive(item: FlowItem<MyData, MyMetadata>): Result<Unit> = runCatching {
        publish(FlowItem(item.pad, item.metadata, MyData(item.value.value * 2)))
    }
}

// A simple sink that prints the received data
class MySink : Sink<MyData, MyMetadata> {
    private val inputMetadata_ = mutableMapOf<UInt, MyMetadata?>()
    override val inputMetadata: Map<UInt, MyMetadata?> get() = inputMetadata_

    override suspend fun receive(item: FlowItem<MyData, MyMetadata>): Result<Unit> = runCatching {
        println("Received $item")
    }

    override fun close() {
        println("Sink closed")
    }

    override fun configure(pad: UInt, metadata: MyMetadata): Result<Unit> {
        inputMetadata_[pad] = metadata
        return Result.success(Unit)
    }
}

fun main() = runBlocking {
    val graph = FlowGraph(Dispatchers.Default) {
        val source = source(MySource(), "source")
        val processor = transformer(MyTransformer(), "transformer")
        val sink = sink(MySink(), "sink")

        connect(source to processor)
        connect(processor to sink)
    }
    graph.source<MySource>("source")!!.impl.run()
    graph.close()
}
