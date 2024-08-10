package dev.silenium.libs.flows.base

import dev.silenium.libs.flows.api.FlowItem
import dev.silenium.libs.flows.api.Source
import dev.silenium.libs.flows.impl.CloningFlow
import java.util.*

/**
 * A base class for [Source] implementations.
 * It provides a [CloningFlow] to publish flow items.
 * It also provides a [outputMetadata_] map to store metadata for each output pad.
 * It implements the [Source] interface.
 * It is an [AutoCloseable] resource.
 */
abstract class SourceBase<T, P> : Source<T, P> {
    override val outputMetadata: Map<UInt, P> get() = outputMetadata_.toMap()
    override val flow = CloningFlow<FlowItem<T, P>>()
    protected val outputMetadata_: MutableMap<UInt, P> = Collections.synchronizedMap(mutableMapOf<UInt, P>())

    protected suspend fun publish(item: FlowItem<T, P>) = flow.publish(item)

    override fun close() {
        flow.close()
    }
}
