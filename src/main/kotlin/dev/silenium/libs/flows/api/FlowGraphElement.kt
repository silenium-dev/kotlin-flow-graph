package dev.silenium.libs.flows.api

import kotlin.reflect.KClass

/**
 * A [FlowGraphElement] is a single element in a [FlowGraph].
 * It wraps the concrete implementation of a [Source], [Sink], or [Transformer].
 */
interface FlowGraphElement<T : Any> : AutoCloseable {
    val name: String
    val type: KClass<T>
    val impl: T

    override fun close()
}

/**
 * A [FlowGraphElement] which is a [Source].
 */
interface SourceFlowGraphElement<T, P, S : Source<T, P>> : FlowGraphElement<S>, Source<T, P>

/**
 * A [FlowGraphElement] which is a [Sink].
 */
interface SinkFlowGraphElement<T, P, S : Sink<T, P>> : FlowGraphElement<S>, Sink<T, P>

/**
 * A [FlowGraphElement] which is a [Transformer].
 */
interface TransformerFlowGraphElement<IT, IP, OT, OP, T : Transformer<IT, IP, OT, OP>> :
    FlowGraphElement<T>, Transformer<IT, IP, OT, OP>
