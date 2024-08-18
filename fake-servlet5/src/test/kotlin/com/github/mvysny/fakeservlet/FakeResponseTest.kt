package com.github.mvysny.fakeservlet

import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
