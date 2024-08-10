package dev.silenium.libs.flows.impl

import dev.silenium.libs.flows.buffer.BufferSink
import dev.silenium.libs.flows.buffer.BufferSource
import dev.silenium.libs.flows.test.Base64Buffer
import dev.silenium.libs.flows.test.Base64Decoder
import dev.silenium.libs.flows.test.DataType
import dev.silenium.libs.flows.test.encodeBase64
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull

class FlowGraphImplTest : FunSpec({
    test("FlowGraphBuilder") {
        val graph = FlowGraph(CoroutineScope(Dispatchers.Default)) {
            val source = source(BufferSource<Base64Buffer, DataType>(0u to DataType.BASE64), "buffer-source")
            val sink = sink(BufferSink<ByteArray, DataType>(), "buffer-sink")
            val decoder = transformer(Base64Decoder(), "base64-decoder")
            connect(source to decoder)
            connect(decoder to sink) { _, _, sourcePad, _ ->
                sourcePad + 1u
            }
        }
        val source = graph.source<BufferSource<Base64Buffer, DataType>>("buffer-source")!!
        val sink = graph.sink<BufferSink<ByteArray, DataType>>("buffer-sink")!!

        val input = "test"
        val inputBuffer = input.encodeBase64()
        source.impl.submit(0u, inputBuffer)
        inputBuffer.close()
        val result = sink.impl.flow.firstOrNull { 1u in it && it[1u]!!.isNotEmpty() }

        graph.close()
        result.shouldNotBeNull()[1u]!!.shouldNotBeEmpty().first().value.decodeToString() shouldBe input
    }
})
