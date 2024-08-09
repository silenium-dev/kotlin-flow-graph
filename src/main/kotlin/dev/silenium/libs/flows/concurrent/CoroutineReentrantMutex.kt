package dev.silenium.libs.flows.concurrent

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Executes the given [block] with this mutex locked.
 * If the mutex is already locked in the current context, the [block] is executed immediately.
 * Otherwise, this function suspends until this mutex is unlocked and then locks it.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T> Mutex.withReentrantLock(crossinline block: suspend () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val key = ReentrantMutexContextKey(this)
    // call block directly when this mutex is already locked in the context
    return if (coroutineContext[key] != null) block()
    else withContext(ReentrantMutexContextElement(key)) { // otherwise, add it to the context and lock the mutex
        withLock { block() }
    }
}

@PublishedApi
internal class ReentrantMutexContextElement(
    override val key: ReentrantMutexContextKey
) : CoroutineContext.Element

@PublishedApi
internal data class ReentrantMutexContextKey(
    val mutex: Mutex
) : CoroutineContext.Key<ReentrantMutexContextElement>
