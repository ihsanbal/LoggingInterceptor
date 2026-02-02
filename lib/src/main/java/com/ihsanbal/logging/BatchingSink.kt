package com.ihsanbal.logging

import java.util.concurrent.ConcurrentHashMap

/**
 * Batches all lines for a request/response and flushes them as one block.
 */
class BatchingSink(
    private val delegate: LogSink
) : LogSink {

    private data class BufferKey(val tag: String)

    private val buffers = ConcurrentHashMap<BufferKey, StringBuilder>()

    override fun log(type: Int, tag: String, message: String) {
        val key = BufferKey(tag)
        val buffer = buffers.getOrPut(key) { StringBuilder() }
        if (buffer.isNotEmpty()) buffer.append('\n')
        buffer.append(message)
    }

    override fun close(type: Int, tag: String) {
        val key = BufferKey(tag)
        buffers.remove(key)?.let { block ->
            delegate.log(type, tag, block.toString())
        }
    }
}
