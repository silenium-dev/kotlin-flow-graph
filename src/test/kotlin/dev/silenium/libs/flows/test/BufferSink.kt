package dev.silenium.libs.flows.test

import dev.silenium.libs.flows.api.FlowItem
import dev.silenium.libs.flows.api.Sink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BufferSink<T, P>(vararg pads: Pair<UInt, P>) : Sink<T, P> {
    private val inputMetadata_: MutableMap<UInt, P?> = pads.toMap().toMutableMap()
    override val inputMetadata: Map<UInt, P?> by ::inputMetadata_

    private val buffer_: MutableMap<UInt, MutableList<FlowItem<T, P>>> = mutableMapOf()
    val buffer: Map<UInt, List<FlowItem<T, P>>> by ::buffer_
    val flow_ = MutableStateFlow<Map<UInt, List<FlowItem<T, P>>>>(emptyMap())
    val flow: StateFlow<Map<UInt, List<FlowItem<T, P>>>> = flow_.asStateFlow()

    override suspend fun submit(item: FlowItem<T, P>): Result<Unit> {
        buffer_.getOrPut(item.pad, ::mutableListOf).add(item)
        flow_.emit(buffer)
        return Result.success(Unit)
    }

    override fun configure(pad: UInt, metadata: P): Result<Unit> {
        check(!inputMetadata.containsKey(pad)) { "pad already configured" }
        inputMetadata_[pad] = metadata
        return Result.success(Unit)
    }

    override fun close() = Unit
}
