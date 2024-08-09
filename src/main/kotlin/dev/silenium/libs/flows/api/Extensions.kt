package dev.silenium.libs.flows.api

import kotlinx.coroutines.Job

interface FlowGraphBuilder : FlowGraph {
    infix fun <T, P> Source<T, P>.connectTo(sink: Sink<T, P>): Result<Job>

    suspend fun finalize(): Result<Unit>
}
