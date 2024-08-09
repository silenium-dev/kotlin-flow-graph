package dev.silenium.libs.flows.api

/**
 * A reference to a resource.
 * The resource is closed when the reference count reaches zero and it implements [AutoCloseable].
 *
 * @param T The type of the underlying resource.
 * @see AutoCloseable
 */
interface Reference<T : Reference<T>> : AutoCloseable {
    /**
     * Creates a new reference to the underlying resource.
     */
    fun clone(): Result<T>

    /**
     * Destroys the reference to the underlying resource.
     * If the reference count reaches zero, the resource is closed.
     */
    override fun close()
}
