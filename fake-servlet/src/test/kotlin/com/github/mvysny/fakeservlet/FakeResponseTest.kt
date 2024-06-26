package com.github.mvysny.fakeservlet

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import javax.servlet.http.Cookie
import kotlin.test.expect

class FakeResponseTest : DynaTest({
    lateinit var request: FakeResponse
    beforeEach { request = FakeResponse() }

    test("smoke") {
        // call stuff and make sure it won't throw
        request.isCommitted
        request.reset()
        request.resetBuffer()
    }

    test("writer") {
        val writer = request.writer
        writer.print("Hello, world!")
        writer.flush()
        expect("Hello, world!") { request.getBufferAsString() }
    }

    test("output stream") {
        request.outputStream.println("Hello, world!")
        expect("Hello, world!\r\n") { request.getBufferAsString() }
    }

    test("headers") {
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

    test("cookies") {
        request.cookies += Cookie("foo", "bar")
        expect("bar") { request.getCookie("foo").value }
        expect(null) { request.findCookie("qqq") }
        expectThrows(IllegalStateException::class, "no such cookie with name baz. Available cookies: foo=bar") {
            request.getCookie("baz")
        }
    }
})
