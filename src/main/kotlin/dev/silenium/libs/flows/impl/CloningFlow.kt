package dev.silenium.libs.flows.impl

import dev.silenium.libs.flows.api.Reference
import dev.silenium.libs.flows.concurrent.withReentrantLock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

/**
 * A [CloningFlow] is a flow that clones each item before publishing it to collectors.
 * Every collector must close its items when it's done with them,
 * as the flow will pass individual instances to each collector.
 *
 * @param T The type of the data.
 * @param wrapped (Optional) the wrapped flow to clone from, can be null to only use manual publishing.
 * @see Flow
 * @see AutoCloseable
 * @see Reference
 */
class CloningFlow<T : Reference<T>>(private val wrapped: Flow<T>? = null) : Flow<T>, AutoCloseable {
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

    /**
     * Publishes a value to all collectors.
     * Each collector will receive a clone of the value, which it must close when it's done with it.
     * *Note: this method is thread-safe*
     *
     * @param value The value to publish.
     * @return A [Unit] result.
     * @see Reference
     */
    suspend fun publish(value: T): Unit = publishLock.withReentrantLock {
        coroutineScope {
            collectors.map { (_, collector) ->
                val item = value.clone().getOrThrow()
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
