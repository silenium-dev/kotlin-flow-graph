package dev.silenium.libs.flows.impl

import dev.silenium.libs.flows.api.ReferenceCounted
import dev.silenium.libs.flows.concurrent.withReentrantLock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

/**
 * Every collector must close its items when it's done with them,
 * as the flow will pass individual instances to each collector.
 */
class CloningFlow<T : ReferenceCounted<T>>(private val wrapped: Flow<T>? = null) : Flow<T>, AutoCloseable {
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
     * *Note: thread-safe*
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
