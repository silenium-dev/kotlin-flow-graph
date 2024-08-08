package dev.silenium.libs.flows.api

data class FlowItem<T, P>(val pad: UInt, val metadata: P, val value: T) : ReferenceCounted<FlowItem<T, P>> {
    @Suppress("UNCHECKED_CAST")
    override fun clone(): Result<FlowItem<T, P>> {
        return when (value) {
            is ReferenceCounted<*> -> value.clone().map { FlowItem(pad, metadata, it as T) }
            else -> Result.success(this)
        }
    }

    override fun close() {
        if (value is AutoCloseable) {
            (value as AutoCloseable).close()
        }
    }
}
