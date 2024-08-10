package dev.silenium.libs.flows.buffer

import dev.silenium.libs.flows.api.FlowItem
import dev.silenium.libs.flows.base.SourceBase

class BufferSource<T, P>(vararg pads: Pair<UInt, P>) : SourceBase<T, P>() {
    override val outputMetadata = pads.toMap()
    suspend fun submit(pad: UInt, value: T) {
        publish(FlowItem(pad, outputMetadata[pad]!!, value))
    }
}
