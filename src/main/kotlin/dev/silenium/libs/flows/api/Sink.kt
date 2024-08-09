package dev.silenium.libs.flows.api

import kotlinx.coroutines.flow.FlowCollector

/**
 * A [Sink] is a flow element that consumes flow items.
 * It can be configured with metadata for each input pad.
 * It can be closed to release resources.
 *
 * @param T The type of the data.
 * @param P The type of the metadata.
 * @see FlowCollector
 * @see AutoCloseable
 * @see FlowItem
 */
interface Sink<T, P> : FlowCollector<FlowItem<T, P>>, AutoCloseable {
    val inputMetadata: Map<UInt, P?>

    fun configure(pad: UInt, metadata: P): Result<Unit>

    suspend fun submit(item: FlowItem<T, P>): Result<Unit>

    override suspend fun emit(value: FlowItem<T, P>) {
        check(inputMetadata.containsKey(value.pad)) { "pad not configured" }
        check(inputMetadata[value.pad] == value.metadata) { "metadata mismatch" }

        submit(value).getOrThrow()
    }
}
