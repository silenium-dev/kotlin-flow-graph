package dev.silenium.libs.flows.api

/**
 * Wraps the actual data of a flow item and contains its metadata and pad id.
 *
 * @param T The type of the data.
 * @param P The type of the metadata.
 * @property pad The pad id of the flow item.
 * @property metadata The metadata of the flow item.
 * @property value The actual data of the flow item.
 * @see Reference
 * @see AutoCloseable
 */
data class FlowItem<T, P>(val pad: UInt, val metadata: P, val value: T) : Reference<FlowItem<T, P>> {
    /**
     * Clones the flow item.
     * If value and/or metadata are [Reference], they are cloned as well.
     */
    @Suppress("UNCHECKED_CAST")
    override fun clone(): Result<FlowItem<T, P>> = runCatching {
        val clonedValue = when (value) {
            is Reference<*> -> value.clone().getOrThrow() as T
            else -> value
        }
        val clonedMetadata = when (metadata) {
            is Reference<*> -> metadata.clone().getOrThrow() as P
            else -> metadata
        }
        FlowItem(pad, clonedMetadata, clonedValue)
    }

    /**
     * Closes the flow item.
     * If value and/or metadata are [AutoCloseable], they are closed as well.
     */
    override fun close() {
        if (value is AutoCloseable) {
            (value as AutoCloseable).close()
        }
        if (metadata is AutoCloseable) {
            (metadata as AutoCloseable).close()
        }
    }
}
