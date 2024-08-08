package dev.silenium.libs.flows.api

import kotlinx.coroutines.flow.FlowCollector

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
