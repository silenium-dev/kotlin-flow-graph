package dev.silenium.libs.flows.api

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface FlowGraphBuilder : FlowGraph {
    infix fun <T, P> Source<T, P>.connectTo(sink: Sink<T, P>): Job =
        flow.onEach(sink::submit).launchIn(this@FlowGraphBuilder)
}
