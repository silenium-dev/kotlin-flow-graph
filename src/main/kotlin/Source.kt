import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.util.*

data class FlowItem<T, P>(val pad: UInt, val metadata: P, val value: T) : ReferenceCounted<FlowItem<T, P>> {
    @Suppress("UNCHECKED_CAST")
    override fun clone(): Result<FlowItem<T, P>> {
        println("Cloning $value")
        println("Is reference counted: ${value is ReferenceCounted<*>}")
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

abstract class SourceBase<T, P> : Source<T, P>, AutoCloseable {
    override val outputMetadata: Map<UInt, P> get() = metadata.toMap()
    override val flow = CloningFlow<FlowItem<T, P>>()
    protected val metadata: MutableMap<UInt, P> = Collections.synchronizedMap(mutableMapOf<UInt, P>())

    protected suspend fun publish(item: FlowItem<T, P>) = flow.publish(item)

    override fun close() {
        flow.close()
    }
}

interface Source<T, P> {
    val outputMetadata: Map<UInt, P>
    val flow: Flow<FlowItem<T, P>>
}

interface Sink<T, P> : FlowCollector<FlowItem<T, P>> {
    val inputMetadata: Map<UInt, P?>

    fun configure(pad: UInt, metadata: P): Result<Unit>

    suspend fun submit(item: FlowItem<T, P>): Result<Unit>

    override suspend fun emit(value: FlowItem<T, P>) {
        check(inputMetadata.containsKey(value.pad)) { "pad not configured" }
        check(inputMetadata[value.pad] == value.metadata) { "metadata mismatch" }

        submit(value).getOrThrow()
    }
}
