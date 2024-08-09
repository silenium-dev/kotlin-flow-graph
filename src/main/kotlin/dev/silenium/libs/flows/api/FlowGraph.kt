package dev.silenium.libs.flows.api

import kotlinx.coroutines.CoroutineScope

/**
 * A [FlowGraph] is a directed graph of [FlowGraphElement]s.
 * It is used to create a flow of data between [Source]s, [Sink]s, and [Transformer]s.
 * The [FlowGraph] is a [CoroutineScope] which will contain all connections between the elements.
 * It is also an [AutoCloseable] to close all elements and cancel all connection jobs when the [FlowGraph] is closed.
 *
 * @see FlowGraphElement
 * @see Source
 * @see Sink
 * @see Transformer
 * @see CoroutineScope
 * @see AutoCloseable
 * @see SourceFlowGraphElement
 * @see SinkFlowGraphElement
 * @see TransformerFlowGraphElement
 * @see FlowGraphConfigScope
 */
interface FlowGraph : AutoCloseable, CoroutineScope {
    /**
     * The list of [FlowGraphElement]s in the [FlowGraph].
     */
    val elements: List<FlowGraphElement<*>>

    /**
     * The number of [FlowGraphElement]s in the [FlowGraph].
     */
    val size: Int

    /**
     * Adds a source to the [FlowGraph].
     * Don't use this for a [Transformer] which is a combined [Source] and [Sink].
     *
     * @param source The [Source] to add to the [FlowGraph].
     * @param name The name of the [SourceFlowGraphElement].
     * @return The [SourceFlowGraphElement] which was added to the [FlowGraph].
     * @see SourceFlowGraphElement
     * @see Source
     */
    fun <T, P, E : Source<T, P>> source(
        source: E,
        name: String = "${source.javaClass.name}-${size}",
    ): SourceFlowGraphElement<T, P, E>

    /**
     * Adds a sink to the [FlowGraph].
     * Don't use this for a [Transformer] which is a combined [Source] and [Sink].
     *
     * @param sink The [Sink] to add to the [FlowGraph].
     * @param name The name of the [SinkFlowGraphElement].
     * @return The [SinkFlowGraphElement] which was added to the [FlowGraph].
     * @see SinkFlowGraphElement
     * @see Sink
     */
    fun <T, P, E : Sink<T, P>> sink(
        sink: E,
        name: String = "${sink.javaClass.name}-${size}",
    ): SinkFlowGraphElement<T, P, E>

    /**
     * Adds a transformer to the [FlowGraph].
     *
     * @param transform The [Transformer] to add to the [FlowGraph].
     * @param name The name of the [TransformerFlowGraphElement].
     * @return The [TransformerFlowGraphElement] which was added to the [FlowGraph].
     * @see TransformerFlowGraphElement
     * @see Transformer
     */
    fun <IT, IP, OT, OP, E : Transformer<IT, IP, OT, OP>> transformer(
        transform: E,
        name: String = "${transform.javaClass.name}-${size}",
    ): TransformerFlowGraphElement<IT, IP, OT, OP, E>

    /**
     * Gets a [SourceFlowGraphElement] by its name.
     *
     * @param name The name of the [SourceFlowGraphElement].
     * @return The [SourceFlowGraphElement] with the given name or `null` if it doesn't exist.
     * @see SourceFlowGraphElement
     */
    fun <E : Source<*, *>> source(name: String): SourceFlowGraphElement<*, *, E>?

    /**
     * Gets a [SinkFlowGraphElement] by its name.
     *
     * @param name The name of the [SinkFlowGraphElement].
     * @return The [SinkFlowGraphElement] with the given name or `null` if it doesn't exist.
     * @see SinkFlowGraphElement
     */
    fun <E : Sink<*, *>> sink(name: String): SinkFlowGraphElement<*, *, E>?

    /**
     * Gets a [TransformerFlowGraphElement] by its name.
     *
     * @param name The name of the [TransformerFlowGraphElement].
     * @return The [TransformerFlowGraphElement] with the given name or `null` if it doesn't exist.
     * @see TransformerFlowGraphElement
     */
    fun <E : Transformer<*, *, *, *>> transformer(name: String): TransformerFlowGraphElement<*, *, *, *, E>?
}
