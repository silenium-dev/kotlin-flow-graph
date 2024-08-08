package dev.silenium.libs.flows.test

import dev.silenium.libs.flows.api.ReferenceCounted
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Base64Buffer : ReferenceCounted<Base64Buffer> {
    private val refCount_: AtomicLong
    val refCount: Long get() = refCount_.get()
    val buffer: ByteBuffer

    private constructor(buffer: ByteBuffer, refCount: AtomicLong) {
        println("Creating cloned buffer")
        this.buffer = buffer
        this.refCount_ = refCount
    }

    constructor(buffer: ByteBuffer) {
        this.buffer = buffer
        this.refCount_ = AtomicLong(1L)
    }

    constructor(content: ByteArray) : this(ByteBuffer.allocateDirect(content.size).apply {
        put(content)
        flip()
    })

    override fun clone(): Result<Base64Buffer> {
        println("Cloning buffer")
        refCount_.incrementAndGet()
        return Result.success(Base64Buffer(buffer, refCount_))
    }

    override fun close() {
        println("Decrementing buffer ref count")
        if (refCount_.decrementAndGet() == 0L) {
            println("Closing buffer")
            buffer.clear()
        }
        println("Buffer ref count: ${refCount_.get()}")
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun String.encodeBase64(): Base64Buffer = Base64Buffer(Base64.encodeToByteArray(this.encodeToByteArray()))
