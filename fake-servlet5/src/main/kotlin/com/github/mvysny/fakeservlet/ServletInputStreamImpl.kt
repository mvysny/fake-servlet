package com.github.mvysny.fakeservlet

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import java.io.ByteArrayInputStream

/**
 * Basic implementation of [ServletInputStream] which only works with in-memory data.
 */
public class ServletInputStreamImpl(public val delegate: ByteArrayInputStream) : ServletInputStream() {
    override fun isFinished(): Boolean = delegate.available() <= 0
    override fun isReady(): Boolean = true
    override fun setReadListener(readListener: ReadListener) {
        throw UnsupportedOperationException("Unsupported")
    }
    override fun read(): Int = delegate.read()
    override fun read(b: ByteArray): Int = delegate.read(b)
    override fun read(
        b: ByteArray,
        off: Int,
        len: Int
    ): Int = delegate.read(b, off, len)

    override fun skip(n: Long): Long = delegate.skip(n)
    override fun available(): Int = delegate.available()
    override fun close() {
        delegate.close()
    }

    override fun mark(readlimit: Int) {
        delegate.mark(readlimit)
    }

    override fun reset() {
        delegate.reset()
    }

    override fun markSupported(): Boolean = delegate.markSupported()
}
