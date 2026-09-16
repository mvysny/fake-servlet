package com.github.mvysny.fakeservlet

import javax.servlet.WriteListener
import javax.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.expect

class FakeResponseTest {
    private lateinit var request: FakeResponse
    @BeforeEach fun setup() { request = FakeResponse() }

    @Test fun smoke() {
        // call stuff and make sure it won't throw
        request.isCommitted
        request.reset()
        request.resetBuffer()
    }

    @Test fun defaults() {
        expect(200) { request.status }
        expect("ISO-8859-1") { request.characterEncoding }
        expect(Locale.US) { request.locale }
        expect(null) { request.contentType }
        expect(4096) { request.bufferSize }
        expect(false) { request.isCommitted }
        expect("") { request.getBufferAsString() }
    }

    @Suppress("DEPRECATION")
    @Test fun setters() {
        request.status = 404
        expect(404) { request.status }
        request.setStatus(500, "boom")
        expect(500) { request.status }
        request.locale = Locale.GERMAN
        expect(Locale.GERMAN) { request.locale }
        request.contentType = "text/plain"
        expect("text/plain") { request.contentType }
        request.bufferSize = 8192
        expect(8192) { request.bufferSize }
        request.setContentLength(5)
        request.setContentLengthLong(5)
    }

    @Suppress("DEPRECATION")
    @Test fun `URLs are not encoded`() {
        expect("/foo") { request.encodeURL("/foo") }
        expect("/foo") { request.encodeUrl("/foo") }
        expect("/foo") { request.encodeRedirectURL("/foo") }
        expect("/foo") { request.encodeRedirectUrl("/foo") }
    }

    @Test fun writer() {
        val writer = request.writer
        writer.print("Hello, world!")
        writer.flush()
        expect("Hello, world!") { request.getBufferAsString() }
    }

    @Test fun `writer needs no flush`() {
        request.writer.print("Hello, ")
        request.writer.print('w')
        request.writer.write("orld!".toCharArray())
        expect("Hello, world!") { request.getBufferAsString() }
    }

    @Test fun `writer is the same instance`() {
        expect(request.writer) { request.writer }
    }

    @Test fun `writer encodes with the character encoding`() {
        request.characterEncoding = "UTF-8"
        request.writer.print("žluť")
        expect("žluť".toByteArray(Charsets.UTF_8).toList()) { request.buffer.toByteArray().toList() }
        expect("žluť") { request.getBufferAsString() }
    }

    @Test fun `setCharacterEncoding() is ignored after getWriter()`() {
        request.writer
        request.characterEncoding = "UTF-8"
        expect("ISO-8859-1") { request.characterEncoding }
    }

    @Test fun `flushBuffer() commits and keeps the content`() {
        request.writer.print("Hello")
        request.flushBuffer()
        expect(true) { request.isCommitted }
        expect("Hello") { request.getBufferAsString() }
    }

    @Test fun sendError() {
        request.sendError(404)
        expect(404) { request.status }
        expect(true) { request.isCommitted }
    }

    @Test fun `sendError() with message`() {
        request.sendError(500, "boom")
        expect(500) { request.status }
        expect(true) { request.isCommitted }
    }

    @Test fun sendRedirect() {
        request.writer.print("discarded")
        request.sendRedirect("/login")
        expect(302) { request.status }
        expect("/login") { request.getHeader("Location") }
        expect(true) { request.isCommitted }
        expect("") { request.getBufferAsString() }
    }

    @Test fun `committed response rejects buffer modifications`() {
        request.flushBuffer()
        assertThrows<IllegalStateException> { request.reset() }
        assertThrows<IllegalStateException> { request.resetBuffer() }
        assertThrows<IllegalStateException> { request.bufferSize = 10 }
        assertThrows<IllegalStateException> { request.sendError(404) }
        assertThrows<IllegalStateException> { request.sendError(404, "not found") }
        assertThrows<IllegalStateException> { request.sendRedirect("/login") }
    }

    @Test fun `reset() clears the buffer, status, headers, cookies and content type`() {
        request.writer.print("Hello")
        request.status = 404
        request.setHeader("foo", "bar")
        request.addCookie(Cookie("foo", "bar"))
        request.contentType = "text/plain"
        request.reset()
        expect("") { request.getBufferAsString() }
        expect(200) { request.status }
        expectList() { request.headerNames.toList() }
        expectList() { request.cookies.toList() }
        expect(null) { request.contentType }
    }

    @Test fun `resetBuffer() keeps status and headers`() {
        request.writer.print("Hello")
        request.status = 404
        request.setHeader("foo", "bar")
        request.resetBuffer()
        expect("") { request.getBufferAsString() }
        expect(404) { request.status }
        expect("bar") { request.getHeader("foo") }
    }

    @Test fun outputStream() {
        request.outputStream.println("Hello, world!")
        expect("Hello, world!\r\n") { request.getBufferAsString() }
        expect(true) { request.outputStream.isReady }
        request.outputStream.setWriteListener(object : WriteListener {
            override fun onWritePossible() {}
            override fun onError(t: Throwable) {}
        })
    }

    @Test fun headers() {
        expect(null) { request.getHeader("foo") }
        expectList() { request.headerNames.toList() }
        expectList() { request.getHeaders("foo").toList() }
        expect(false) { request.containsHeader("foo") }
        request.setHeader("foo", "bar")
        expect("bar") { request.getHeader("foo") }
        expect(true) { request.containsHeader("foo") }
        expectList("foo") { request.headerNames.toList() }
        expectList("bar") { request.getHeaders("foo").toList() }
        request.headers["foo"] = arrayOf("bar", "baz")
        expect("bar") { request.getHeader("foo") }
        expectList("foo") { request.headerNames.toList() }
        expectList("bar", "baz") { request.getHeaders("foo").toList() }
    }

    @Test fun `addHeader() appends, setHeader() replaces`() {
        request.addHeader("foo", "1")
        request.addIntHeader("foo", 2)
        request.addDateHeader("foo", 3L)
        expectList("1", "2", "3") { request.getHeaders("foo").toList() }
        request.setIntHeader("foo", 4)
        expectList("4") { request.getHeaders("foo").toList() }
        request.setDateHeader("foo", 5L)
        expectList("5") { request.getHeaders("foo").toList() }
    }

    @Test fun `headers are case-insensitive`() {
        request.setHeader("Content-Language", "en")
        expect("en") { request.getHeader("content-language") }
        expectList("en") { request.getHeaders("CONTENT-LANGUAGE").toList() }
        expect(true) { request.containsHeader("content-LANGUAGE") }
        request.addHeader("content-language", "de")
        expectList("en", "de") { request.getHeaders("Content-Language").toList() }
        request.setHeader("CONTENT-LANGUAGE", "fi")
        expectList("CONTENT-LANGUAGE") { request.headerNames.toList() }
        expectList("fi") { request.getHeaders("content-language").toList() }
    }

    @Test fun cookies() {
        request.cookies += Cookie("foo", "bar")
        request.addCookie(Cookie("baz", "qux"))
        expect("bar") { request.getCookie("foo").value }
        expect("qux") { request.getCookie("baz").value }
        expect(null) { request.findCookie("qqq") }
        val ex = assertThrows<IllegalStateException> {
            request.getCookie("qqq")
        }
        expect("no such cookie with name qqq. Available cookies: foo=bar, baz=qux") { ex.message }
    }
}
