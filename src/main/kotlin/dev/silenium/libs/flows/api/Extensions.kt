package dev.silenium.libs.flows.api

import kotlinx.coroutines.Job

/**
 * ConfigScope for creating connections between [Source]s and [Sink]s in a [FlowGraph].
 */
interface FlowGraphConfigScope : FlowGraph {
    /**
     * Creates a connection job in the [kotlinx.coroutines.CoroutineScope] of the [FlowGraph].
     * The job is started immediately.
     */
    infix fun <T, P> Source<T, P>.connectTo(sink: Sink<T, P>): Result<Job>

    /**
     * Configures the [FlowGraph].
     * Currently, it does:
     * - wait for all connection jobs to be started
     */
    suspend fun configure(): Result<Unit>
}
