package com.github.mvysny.fakeservlet

import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * A response that keeps a [buffer] of written data.
 */
public open class FakeResponse : HttpServletResponse {
    override fun encodeURL(url: String): String = url

    @Deprecated("Deprecated in Java")
    override fun encodeUrl(url: String): String = encodeURL(url)

    public val headers: ConcurrentHashMap<String, Array<String>> = ConcurrentHashMap<String, Array<String>>()

    override fun addIntHeader(name: String, value: Int) {
        addHeader(name, value.toString())
    }

    public val cookies: CopyOnWriteArrayList<Cookie> = CopyOnWriteArrayList<Cookie>()

    override fun addCookie(cookie: Cookie) {
        cookies.add(cookie)
    }

    public fun getCookie(name: String): Cookie = checkNotNull(findCookie(name)) {
        "no such cookie with name $name. Available cookies: ${cookies.joinToString { "${it.name}=${it.value}" }}"
    }

    public fun findCookie(name: String): Cookie? = cookies.firstOrNull { it.name == name }

    @Deprecated("Deprecated in Java")
    override fun encodeRedirectUrl(url: String): String = encodeRedirectURL(url)

    override fun flushBuffer() {
        _committed = true
    }

    override fun encodeRedirectURL(url: String): String = url

    override fun sendRedirect(location: String) {
        resetBuffer()
        log.info("sendRedirect($location)")
        _status = HttpServletResponse.SC_FOUND
        setHeader("Location", location)
        _committed = true
    }

    public val buffer: ByteArrayOutputStream = ByteArrayOutputStream()

    /**
     * The target of the last [FakeRequestDispatcher.forward].
     */
    public var forwardedUrl: String? = null

    /**
     * The targets of [FakeRequestDispatcher.include], in call order.
     */
    public val includedUrls: MutableList<String> = CopyOnWriteArrayList()

    public var _bufferSize: Int = 4096

    override fun setBufferSize(size: Int) {
        checkNotCommitted()
        _bufferSize = size
    }

    public var _locale: Locale = Locale.US

    override fun getLocale(): Locale = _locale

    override fun sendError(sc: Int, msg: String?) {
        resetBuffer()
        log.error("The app requested to send an error: sendError($sc, $msg)")
        _status = sc
        _committed = true
    }

    override fun sendError(sc: Int) {
        resetBuffer()
        log.error("The app requested to send an error: sendError($sc)")
        _status = sc
        _committed = true
    }

    override fun setContentLengthLong(len: Long) {
    }

    public var _characterEncoding: String = "ISO-8859-1"

    /**
     * Ignored once [getWriter] was called, as the spec says: the writer already encodes with the old charset.
     */
    override fun setCharacterEncoding(charset: String) {
        if (_writer == null) {
            _characterEncoding = charset
        }
    }

    override fun addDateHeader(name: String, date: Long) {
        addHeader(name, date.toString())
    }

    override fun setLocale(loc: Locale) {
        _locale = loc
    }

    override fun getHeaders(name: String): Collection<String> = findHeader(name)?.toList() ?: listOf()

    /**
     * HTTP header names are case-insensitive, while [headers] keys are not.
     */
    private fun findHeaderName(name: String): String? =
        if (headers.containsKey(name)) name else headers.keys.firstOrNull { it.equals(name, ignoreCase = true) }

    private fun findHeader(name: String): Array<String>? = findHeaderName(name)?.let { headers[it] }

    override fun addHeader(name: String, value: String) {
        headers.compute(findHeaderName(name) ?: name) { _, v-> (v ?: arrayOf()) + value }
    }

    override fun setContentLength(len: Int) {
    }

    override fun getBufferSize(): Int = _bufferSize

    private fun checkNotCommitted() {
        check(!_committed) { "Already committed" }
    }

    override fun resetBuffer() {
        checkNotCommitted()
        buffer.reset()
    }

    /**
     * Clears the buffer, the status, headers, cookies and the content type, as the spec says.
     */
    override fun reset() {
        checkNotCommitted()
        buffer.reset()
        _status = HttpServletResponse.SC_OK
        headers.clear()
        cookies.clear()
        _contentType = null
    }

    override fun setDateHeader(name: String, date: Long) {
        setHeader(name, date.toString())
    }

    public var _status: Int = 200

    override fun getStatus(): Int = _status

    override fun getCharacterEncoding(): String = _characterEncoding

    public var _committed: Boolean = false

    override fun isCommitted(): Boolean {
        // https://github.com/mvysny/karibu-testing/issues/174
        return _committed
    }

    override fun setStatus(sc: Int) {
        _status = sc
    }

    @Deprecated("Deprecated in Java")
    override fun setStatus(sc: Int, sm: String?) {
        _status = sc
    }

    override fun getHeader(name: String): String? = findHeader(name)?.get(0)

    public var _contentType: String? = null

    override fun getContentType(): String? = _contentType

    private var _writer: PrintWriter? = null

    /**
     * Returns the same writer on every call; it flushes after every write so [buffer] is always up to date.
     */
    override fun getWriter(): PrintWriter {
        if (_writer == null) {
            _writer = object : PrintWriter(OutputStreamWriter(buffer, _characterEncoding), true) {
                override fun write(c: Int) {
                    super.write(c)
                    flush()
                }

                override fun write(buf: CharArray, off: Int, len: Int) {
                    super.write(buf, off, len)
                    flush()
                }

                override fun write(s: String, off: Int, len: Int) {
                    super.write(s, off, len)
                    flush()
                }
            }
        }
        return _writer!!
    }

    override fun containsHeader(name: String): Boolean = findHeaderName(name) != null

    override fun setIntHeader(name: String, value: Int) {
        setHeader(name, value.toString())
    }

    override fun getHeaderNames(): Collection<String> = headers.keys.toSet()

    override fun setHeader(name: String, value: String) {
        headers.keys.removeIf { it.equals(name, ignoreCase = true) }
        headers[name] = arrayOf(value)
    }

    override fun getOutputStream(): ServletOutputStream = object : ServletOutputStream() {
        override fun write(b: Int) {
            buffer.write(b)
        }

        override fun isReady(): Boolean = true

        override fun setWriteListener(writeListener: WriteListener) {}
    }

    override fun setContentType(type: String?) {
        _contentType = type
    }

    public fun getBufferAsString(): String = String(buffer.toByteArray(), Charset.forName(_characterEncoding))

    public companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(FakeResponse::class.java)
    }
}
