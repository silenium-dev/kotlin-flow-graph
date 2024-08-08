package dev.silenium.libs.flows.api

import kotlinx.coroutines.CoroutineScope

interface FlowGraph : AutoCloseable, CoroutineScope {
    val elements: List<FlowGraphElement<*>>
    val size: Int

    fun <T, P, E : Source<T, P>> source(
        source: E,
        name: String = "${source.javaClass.name}-${size}",
    ): SourceFlowGraphElement<T, P, E>

    fun <T, P, E : Sink<T, P>> sink(
        sink: E,
        name: String = "${sink.javaClass.name}-${size}",
    ): SinkFlowGraphElement<T, P, E>

    fun <IT, IP, OT, OP, E : Transformer<IT, IP, OT, OP>> transformer(
        transform: E,
        name: String = "${transform.javaClass.name}-${size}",
    ): TransformerFlowGraphElement<IT, IP, OT, OP, E>

    fun <E : Source<*, *>> source(name: String): FlowGraphElement<E>?
    fun <E : Sink<*, *>> sink(name: String): FlowGraphElement<E>?
    fun <E : Transformer<*, *, *, *>> transformer(name: String): FlowGraphElement<E>?
}
