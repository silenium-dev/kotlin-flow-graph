package dev.silenium.libs.flows.api

import kotlinx.coroutines.flow.Flow

/**
 * A [Source] is a flow element that produces flow items.
 * It can be queried for metadata for each output pad.
 * It can be closed to release resources.
 *
 * @param T The type of the data.
 * @param P The type of the metadata.
 * @see AutoCloseable
 * @see FlowItem
 * @see Flow
 */
interface Source<T, P> : AutoCloseable {
    val outputMetadata: Map<UInt, P>
    val flow: Flow<FlowItem<T, P>>
}
