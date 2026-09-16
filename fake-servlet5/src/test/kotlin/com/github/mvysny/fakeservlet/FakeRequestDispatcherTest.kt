package com.github.mvysny.fakeservlet

import jakarta.servlet.http.HttpServletResponseWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.expect

class FakeRequestDispatcherTest {
    private val request: FakeRequest = FakeRequest(FakeHttpSession.create(FakeContext()))
    private val response: FakeResponse = FakeResponse()

    @Test fun defaults() {
        expect(null) { response.forwardedUrl }
        expectList() { response.includedUrls }
    }

    @Test fun forward() {
        response.writer.print("discarded")
        response.writer.flush()
        request.getRequestDispatcher("/login.jsp")!!.forward(request, response)
        expect("/login.jsp") { response.forwardedUrl }
        expect("") { response.getBufferAsString() }
        expect(true) { response.isCommitted }
    }

    @Test fun `forward fails on a committed response`() {
        response.flushBuffer()
        assertThrows<IllegalStateException> { FakeRequestDispatcher("/foo").forward(request, response) }
        expect(null) { response.forwardedUrl }
    }

    @Test fun include() {
        FakeRequestDispatcher("/header.jsp").include(request, response)
        FakeRequestDispatcher("/footer.jsp").include(request, response)
        expectList("/header.jsp", "/footer.jsp") { response.includedUrls }
        expect(false) { response.isCommitted }
    }

    @Test fun `unwraps response wrappers`() {
        val wrapper = HttpServletResponseWrapper(HttpServletResponseWrapper(response))
        FakeRequestDispatcher("/header.jsp").include(request, wrapper)
        FakeRequestDispatcher("/foo").forward(request, wrapper)
        expectList("/header.jsp") { response.includedUrls }
        expect("/foo") { response.forwardedUrl }
    }
}
