import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

class CloningFlow<T>(private val wrapped: Flow<T>? = null) : Flow<T>, AutoCloseable {
    private val idCounter = AtomicLong(0L)
    private val collectors = ConcurrentHashMap<Long, FlowCollector<T>>()
    private val finished = CompletableDeferred<Unit>()
    private val publishLock = Mutex()

    private val job = wrapped?.let {
        CoroutineScope(Dispatchers.Default).launch {
            wrapped.collect(::publish)
        }.also {
            it.invokeOnCompletion {
                finished.complete(Unit)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun publish(value: T): Unit = publishLock.withReentrantLock {
        coroutineScope {
            collectors.map { (_, collector) ->
                val item = when {
                    value is ReferenceCounted<*> -> value.clone().getOrThrow() as T
                    else -> value
                }
                launch {
                    collector.emit(item)
                }
            }.joinAll()
        }
    }

    override suspend fun collect(collector: FlowCollector<T>): Unit = coroutineScope {
        val id = idCounter.getAndIncrement()
        collectors[id] = collector
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { collectors.remove(id) }
            finished.invokeOnCompletion {
                collectors.remove(id)
                continuation.resume(Unit)
            }
        }
    }

    override fun close() {
        if (job != null) {
            runBlocking { job.cancelAndJoin() }
        } else {
            finished.complete(Unit)
        }
    }
}
