package dev.silenium.libs.flows.api

import kotlin.reflect.KClass

interface FlowGraphElement<T : Any> : AutoCloseable {
    val name: String
    val type: KClass<T>
    val impl: T

    override fun close()
}

interface SourceFlowGraphElement<T, P, S : Source<T, P>> : FlowGraphElement<S>, Source<T, P>
interface SinkFlowGraphElement<T, P, S : Sink<T, P>> : FlowGraphElement<S>, Sink<T, P>
interface TransformerFlowGraphElement<IT, IP, OT, OP, T : Transformer<IT, IP, OT, OP>> : FlowGraphElement<T>, Transformer<IT, IP, OT, OP>
