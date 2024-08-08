interface ReferenceCounted<T : AutoCloseable> : AutoCloseable {
    fun clone(): Result<T>
}
