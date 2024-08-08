package dev.silenium.libs.flows.api

import kotlinx.coroutines.flow.Flow

interface Source<T, P> : AutoCloseable {
    val outputMetadata: Map<UInt, P>
    val flow: Flow<FlowItem<T, P>>
}
