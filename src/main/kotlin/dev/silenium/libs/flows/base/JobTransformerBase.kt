package dev.silenium.libs.flows.base

import dev.silenium.libs.flows.api.Transformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

abstract class JobTransformerBase<IT, IP, OT, OP>(
    protected val coroutineScope: CoroutineScope,
    protected val pads: Set<UInt>? = setOf(0u)
) : Transformer<IT, IP, OT, OP>, SourceBase<OT, OP>() {

    @OptIn(ExperimentalUnsignedTypes::class)
    constructor(coroutineScope: CoroutineScope, vararg inputPads: UInt) : this(coroutineScope, inputPads.toSet())

    protected val inputMetadata_ = mutableMapOf<UInt, IP>()
    override val inputMetadata: Map<UInt, IP?> = Collections.unmodifiableMap(inputMetadata_)
    protected var job: Job? = null

    override fun configure(pad: UInt, metadata: IP): Result<Unit> {
        if (!inputMetadata_.containsKey(pad)) return Result.failure(IllegalStateException("pad already configured"))
        if (pads?.contains(pad) == false) return Result.failure(IllegalStateException("pad not allowed"))

        inputMetadata_[pad] = metadata
        outputMetadata_[pad] = outputMetadata(metadata)

        if (pads?.equals(inputMetadata_.keys) == true) {
            job = coroutineScope.launch { run() }
        }

        return Result.success(Unit)
    }

    override fun close() {
        runBlocking { job?.join() }
        super.close()
    }

    abstract fun outputMetadata(inputMetadata: IP): OP

    /**
     * main processing function
     *
     * *Note: cancellation must be handled by the implementation
     * ([close] only joins the job, it does not cancel it)*
     * @see CoroutineScope
     */
    protected abstract suspend fun CoroutineScope.run()
}
