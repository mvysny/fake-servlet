package com.github.mvysny.fakeservlet

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.servlet.http.Cookie
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

    @Test fun outputStream() {
        request.outputStream.println("Hello, world!")
        expect("Hello, world!\r\n") { request.getBufferAsString() }
    }

    @Test fun headers() {
        expect(null) { request.getHeader("foo") }
        expectList() { request.headerNames.toList() }
        expectList() { request.getHeaders("foo").toList() }
        request.setHeader("foo", "bar")
        expect("bar") { request.getHeader("foo") }
        expectList("foo") { request.headerNames.toList() }
        expectList("bar") { request.getHeaders("foo").toList() }
        request.headers["foo"] = arrayOf("bar", "baz")
        expect("bar") { request.getHeader("foo") }
        expectList("foo") { request.headerNames.toList() }
        expectList("bar", "baz") { request.getHeaders("foo").toList() }
    }

    @Test fun cookies() {
        request.cookies += Cookie("foo", "bar")
        expect("bar") { request.getCookie("foo").value }
        expect(null) { request.findCookie("qqq") }
        val ex = assertThrows<IllegalStateException> {
            request.getCookie("baz")
        }
        expect("no such cookie with name baz. Available cookies: foo=bar") { ex.message }
    }
}
