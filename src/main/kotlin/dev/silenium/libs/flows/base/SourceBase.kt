package dev.silenium.libs.flows.base

import dev.silenium.libs.flows.api.FlowItem
import dev.silenium.libs.flows.api.Source
import dev.silenium.libs.flows.impl.CloningFlow
import java.util.*

abstract class SourceBase<T, P> : Source<T, P> {
    override val outputMetadata: Map<UInt, P> get() = metadata.toMap()
    override val flow = CloningFlow<FlowItem<T, P>>()
    protected val metadata: MutableMap<UInt, P> = Collections.synchronizedMap(mutableMapOf<UInt, P>())

    protected suspend fun publish(item: FlowItem<T, P>) = flow.publish(item)

    override fun close() {
        flow.close()
    }
}
