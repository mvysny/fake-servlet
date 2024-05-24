package com.github.mvysny.fakeservlet

import java.io.IOException
import java.io.PrintWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

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
        buffer.reset()
        _committed = true
    }

    override fun encodeRedirectURL(url: String): String = url

    override fun sendRedirect(location: String) {
        resetBuffer()
        log.error("sendRedirect($location)")
    }

    public val buffer: ByteArrayOutputStream = ByteArrayOutputStream()

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
        _committed = true
    }

    override fun sendError(sc: Int) {
        resetBuffer()
        log.error("The app requested to send an error: sendError($sc)")
        _committed = true
    }

    override fun setContentLengthLong(len: Long) {
    }

    public var _characterEncoding: String = "ISO-8859-1"

    override fun setCharacterEncoding(charset: String) {
        _characterEncoding = charset
    }

    override fun addDateHeader(name: String, date: Long) {
        addHeader(name, date.toString())
    }

    override fun setLocale(loc: Locale) {
        _locale = loc
    }

    override fun getHeaders(name: String): Collection<String> = headers[name]?.toList() ?: listOf()

    override fun addHeader(name: String, value: String) {
        headers.compute(name) { _, v-> (v ?: arrayOf()) + value }
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

    override fun reset() {
        checkNotCommitted()
        buffer.reset()
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

    override fun getHeader(name: String): String? = headers[name]?.get(0)

    public var _contentType: String? = null

    override fun getContentType(): String? = _contentType

    override fun getWriter(): PrintWriter = PrintWriter(OutputStreamWriter(buffer, _characterEncoding))

    override fun containsHeader(name: String): Boolean = headers.containsKey(name)

    override fun setIntHeader(name: String, value: Int) {
        setHeader(name, value.toString())
    }

    override fun getHeaderNames(): Collection<String> = headers.keys.toSet()

    override fun setHeader(name: String, value: String) {
        headers[name] = arrayOf(value)
    }

    override fun getOutputStream(): ServletOutputStream {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setContentType(type: String?) {
        _contentType = type
    }
    
    public companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(FakeResponse::class.java)
    }
}
