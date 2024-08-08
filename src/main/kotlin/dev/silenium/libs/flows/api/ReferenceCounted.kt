package dev.silenium.libs.flows.api

interface ReferenceCounted<T : AutoCloseable> : AutoCloseable {
    fun clone(): Result<T>
}
