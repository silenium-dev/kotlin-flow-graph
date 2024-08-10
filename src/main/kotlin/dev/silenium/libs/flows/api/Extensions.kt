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
    fun <T, P> connect(
        pair: Pair<Source<T, P>, Sink<T, P>>,
        padSelector: (sourceSinkMap: Map<UInt, UInt>, sourcePads: Map<UInt, P>, sourcePad: UInt, metadata: P) -> UInt? = { _, _, pad, _ -> pad },
    ): Job

    /**
     * Configures the [FlowGraph].
     * Currently, it does:
     * - wait for all connection jobs to be started
     */
    suspend fun configure(): Result<FlowGraph>
}
