package dev.silenium.libs.flows.impl

import dev.silenium.libs.flows.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

internal class FlowGraphImpl(private val coroutineScope: CoroutineScope) :
    FlowGraph, CoroutineScope by coroutineScope {
    override val elements = mutableListOf<FlowGraphElement<*>>()
    override val size: Int by elements::size

    constructor(coroutineContext: CoroutineContext) : this(CoroutineScope(coroutineContext))

    private data class SourceFlowGraphElementImpl<T, P, S : Source<T, P>>(
        override val name: String,
        override val impl: S,
        override val type: KClass<S> = impl.javaClass.kotlin,
    ) : SourceFlowGraphElement<T, P, S>, Source<T, P> by impl

    private data class SinkFlowGraphElementImpl<T, P, S : Sink<T, P>>(
        override val name: String,
        override val impl: S,
        override val type: KClass<S> = impl.javaClass.kotlin,
    ) : SinkFlowGraphElement<T, P, S>, Sink<T, P> by impl

    private data class TransformerFlowGraphElementImpl<IT, IP, OT, OP, T : Transformer<IT, IP, OT, OP>>(
        override val name: String,
        override val impl: T,
        override val type: KClass<T> = impl.javaClass.kotlin,
    ) : TransformerFlowGraphElement<IT, IP, OT, OP, T>, Transformer<IT, IP, OT, OP> by impl

    override fun <T, P, E : Source<T, P>> source(
        source: E,
        name: String,
    ): SourceFlowGraphElement<T, P, E> {
        val element = SourceFlowGraphElementImpl(name, source)
        elements.add(element)
        return element
    }

    override fun <T, P, E : Sink<T, P>> sink(
        sink: E,
        name: String,
    ): SinkFlowGraphElement<T, P, E> {
        val element = SinkFlowGraphElementImpl(name, sink)
        elements.add(element)
        return element
    }

    override fun <IT, IP, OT, OP, E : Transformer<IT, IP, OT, OP>> transformer(
        transform: E,
        name: String,
    ): TransformerFlowGraphElement<IT, IP, OT, OP, E> {
        val element = TransformerFlowGraphElementImpl(name, transform)
        elements.add(element)
        return element
    }

    override fun <S : Sink<*, *>> sink(name: String): FlowGraphElement<S>? {
        val element = elements.find { it.name == name } ?: return null
        if (element is SinkFlowGraphElement<*, *, *>) {
            @Suppress("UNCHECKED_CAST")
            return element as FlowGraphElement<S>
        }
        throw IllegalArgumentException("Element $name is not a sink")
    }

    override fun <S : Source<*, *>> source(name: String): FlowGraphElement<S>? {
        val element = elements.find { it.name == name } ?: return null
        if (element is SourceFlowGraphElement<*, *, *>) {
            @Suppress("UNCHECKED_CAST")
            return element as FlowGraphElement<S>
        }
        throw IllegalArgumentException("Element $name is not a source")
    }

    override fun <T : Transformer<*, *, *, *>> transformer(name: String): FlowGraphElement<T>? {
        val element = elements.find { it.name == name } ?: return null
        if (element is TransformerFlowGraphElement<*, *, *, *, *>) {
            @Suppress("UNCHECKED_CAST")
            return element as FlowGraphElement<T>
        }
        throw IllegalArgumentException("Element $name is not a transformer")
    }

    override fun close() {
        cancel("FlowGraph closed")
        elements.forEach(AutoCloseable::close)
    }

    object CoroutineContextKey : CoroutineContext.Key<CoroutineContextElement>
    data class CoroutineContextElement(
        val flowGraph: FlowGraph,
        override val key: CoroutineContext.Key<*> = CoroutineContextKey
    ) : CoroutineContext.Element
}

internal class FlowGraphBuilderImpl(private val flowGraph: FlowGraph) : FlowGraphBuilder, FlowGraph by flowGraph

internal fun FlowGraph.builder() = FlowGraphBuilderImpl(this)

fun FlowGraph(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    block: FlowGraphBuilder.() -> Unit,
): FlowGraph = FlowGraphImpl(coroutineContext).builder().apply(block)

fun FlowGraph(
    coroutineScope: CoroutineScope,
    block: FlowGraphBuilder.() -> Unit,
): FlowGraph = FlowGraphImpl(coroutineScope).builder().apply(block)
