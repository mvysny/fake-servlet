package com.github.mvysny.fakeservlet

import javax.servlet.DispatcherType
import javax.servlet.http.Cookie
import javax.servlet.http.Part
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.Proxy
import java.util.*
import kotlin.test.assertSame
import kotlin.test.expect

class FakeRequestTest {
    private lateinit var request: FakeRequest
    @BeforeEach fun setUp() { request = FakeRequest(FakeHttpSession.create(FakeContext())) }

    @Test fun attributes() {
        expect(null) { request.getAttribute("foo") }
        expectList() { request.attributeNames.toList() }
        request.setAttribute("foo", "bar")
        expect("bar") { request.getAttribute("foo") }
        expectList("foo") { request.attributeNames.toList() }
        request.setAttribute("foo", null)
        expect(null) { request.getAttribute("foo") }
        request.setAttribute("foo", "bar")
        expect("bar") { request.getAttribute("foo") }
        request.removeAttribute("foo")
        expect(null) { request.getAttribute("foo") }
        expectList() { request.attributeNames.toList() }
    }

    @Test fun parameters() {
        expect(null) { request.getParameter("foo") }
        expectList() { request.parameterNames.toList() }
        expect(null) { request.getParameterValues("foo") }
        request.setParameter("foo", "bar")
        expect("bar") { request.getParameter("foo") }
        expectList("foo") { request.parameterNames.toList() }
        expectList("bar") { request.getParameterValues("foo")!!.toList() }
        request.parameters["foo"] = arrayOf("bar", "baz")
        expect("bar") { request.getParameter("foo") }
        expectList("foo") { request.parameterNames.toList() }
        expectList("bar", "baz") { request.getParameterValues("foo")!!.toList() }
        expect(setOf("foo")) { request.parameterMap.keys }
        expectList("bar", "baz") { request.parameterMap["foo"]!!.toList() }
    }

    @Test fun `setParameter() with multiple values`() {
        request.setParameter("foo", "bar", "baz")
        expectList("bar", "baz") { request.getParameterValues("foo")!!.toList() }
    }

    @Test fun defaults() {
        expect("HTTP/1.1") { request.protocol }
        expect("GET") { request.method }
        expect("http") { request.scheme }
        expect("/") { request.requestURI }
        expect("http://localhost:8080/") { request.requestURL.toString() }
        expect("") { request.contextPath }
        expect("") { request.servletPath }
        expect(null) { request.pathInfo }
        expect(null) { request.queryString }
        expect(null) { request.contentType }
        expect(-1) { request.contentLength }
        expect(-1L) { request.contentLengthLong }
        expect(DispatcherType.REQUEST) { request.dispatcherType }
        expect(false) { request.isAsyncStarted }
        expect(false) { request.isAsyncSupported }
        expect(null) { request.remoteUser }
        expect("127.0.0.1") { request.serverName }
        expect("127.0.0.1") { request.localAddr }
        expect("127.0.0.1") { request.remoteHost }
        expect("localhost") { request.localName }
        expect(-1L) { request.getDateHeader("If-Modified-Since") }
        expect(request.session.id) { request.requestedSessionId }
        expect(false) { request.isRequestedSessionIdFromCookie }
        expect(false) { request.isRequestedSessionIdFromURL }
        expect(request.session.servletContext) { request.servletContext }
        expect(null) { request.pathTranslated }
    }

    @Test fun `getRequestDispatcher() resolves relative paths`() {
        expect(null) { request.getRequestDispatcher(null) }
        expect("/foo/bar") { (request.getRequestDispatcher("/foo/bar") as FakeRequestDispatcher).target }
        expect("/bar") { (request.getRequestDispatcher("bar") as FakeRequestDispatcher).target }
        val nested = object : FakeRequest(request.session) {
            override fun getServletPath(): String = "/app"
            override fun getPathInfo(): String = "/foo/baz"
        }
        expect("/app/foo/bar") { (nested.getRequestDispatcher("bar") as FakeRequestDispatcher).target }
    }

    @Test fun `async is not supported`() {
        assertThrows<IllegalStateException> { request.startAsync() }
        assertThrows<IllegalStateException> { request.startAsync(request, FakeResponse()) }
    }

    @Suppress("DEPRECATION")
    @Test fun `getRealPath() delegates to the context`() {
        (request.servletContext as FakeContext).realPathRoots = listOf("src/test/webapp")
        expect(request.servletContext.getRealPath("/VAADIN/themes/default/img/1.txt")) { request.getRealPath("/VAADIN/themes/default/img/1.txt") }
        expect(null) { request.getRealPath("/nonexisting.txt") }
        expect(null) { request.getRealPath(null) }
    }

    @Test fun isRequestedSessionIdValid() {
        expect(true) { request.isRequestedSessionIdValid }
        request.session.invalidate()
        expect(false) { request.isRequestedSessionIdValid }
    }

    @Test fun `values from FakeHttpEnvironment`() {
        expect(8080) { request.serverPort }
        expect(8080) { request.localPort }
        expect(8080) { request.remotePort }
        expect("127.0.0.1") { request.remoteAddr }
        expect(null) { request.authType }
        expect(false) { request.isSecure }
        try {
            FakeHttpEnvironment.serverPort = 1
            FakeHttpEnvironment.localPort = 2
            FakeHttpEnvironment.remotePort = 3
            FakeHttpEnvironment.remoteAddr = "10.0.0.1"
            FakeHttpEnvironment.authType = "BASIC"
            FakeHttpEnvironment.isSecure = true
            expect(1) { request.serverPort }
            expect(2) { request.localPort }
            expect(3) { request.remotePort }
            expect("10.0.0.1") { request.remoteAddr }
            expect("BASIC") { request.authType }
            expect(true) { request.isSecure }
        } finally {
            FakeHttpEnvironment.serverPort = 8080
            FakeHttpEnvironment.localPort = 8080
            FakeHttpEnvironment.remotePort = 8080
            FakeHttpEnvironment.remoteAddr = "127.0.0.1"
            FakeHttpEnvironment.authType = null
            FakeHttpEnvironment.isSecure = false
        }
    }

    @Test fun authenticate() {
        assertThrows<UnsupportedOperationException> { request.authenticate(FakeResponse()) }
        val original = FakeHttpEnvironment.authenticator
        try {
            FakeHttpEnvironment.authenticator = { response -> response.status = 401; false }
            val response = FakeResponse()
            expect(false) { request.authenticate(response) }
            expect(401) { response.status }
        } finally {
            FakeHttpEnvironment.authenticator = original
        }
    }

    @Test fun `getSession(false) returns the old invalid session`() {
        val session = request.session as FakeHttpSession
        expect(true) { session.isValid }
        session.setAttribute("foo", "bar")
        session.invalidate()
        expect(session) { request.getSession(false) }
        expect(false) { session.isValid }
    }

    @Test fun `getSession(false) returns null on invalid session with strict checks`() {
        request.session.invalidate()
        FakeHttpEnvironment.strictSessionValidityChecks = true
        try {
            expect(null) { request.getSession(false) }
        } finally {
            FakeHttpEnvironment.strictSessionValidityChecks = false
        }
    }

    @Test fun characterEncoding() {
        expect(null) { request.characterEncoding }
        request.setCharacterEncoding("UTF-8")
        expect("UTF-8") { request.characterEncoding }
    }

    @Test fun `headers are case-insensitive`() {
        expect("IntelliJ IDEA/182.4892.20") { request.getHeader("User-Agent") }
        expectList("IntelliJ IDEA/182.4892.20") { request.getHeaders("USER-AGENT").toList() }
        request.headers["X-Count"] = listOf("5")
        expect(5) { request.getIntHeader("x-count") }
        expect(null) { request.getHeader("foo") }
        expectList() { request.getHeaders("foo").toList() }
    }

    @Test fun `header names and missing int header`() {
        expectList("user-agent") { request.headerNames.toList() }
        expect(-1) { request.getIntHeader("X-Count") }
    }

    @Test fun locale() {
        expect(Locale.US) { request.locale }
        expectList(Locale.US) { request.locales.toList() }
        request.localeInt = Locale.GERMAN
        expect(Locale.GERMAN) { request.locale }
        expectList(Locale.GERMAN) { request.locales.toList() }
    }

    @Test fun cookies() {
        expect(null) { request.cookies }
        request.addCookie(Cookie("foo", "bar"))
        request.addCookie(Cookie("baz", "qux"))
        expectList("foo", "baz") { request.cookies!!.map { it.name } }
    }

    @Test fun parts() {
        assertThrows<IllegalStateException> { request.parts }
        assertThrows<IllegalStateException> { request.getPart("foo") }
        val part = Proxy.newProxyInstance(Part::class.java.classLoader, arrayOf(Part::class.java)) { _, method, _ ->
            if (method.name == "getName") "foo" else throw UnsupportedOperationException(method.name)
        } as Part
        request.partsInt = mutableListOf(part)
        assertSame(part, request.getPart("foo"))
        expect(null) { request.getPart("bar") }
        expect(1) { request.parts.size }
    }

    @Test fun `getSession(true) creates a new session when invalidated`() {
        var session = request.session as FakeHttpSession
        expect(true) { session.isValid }
        session.setAttribute("foo", "bar")
        session.invalidate()
        expect(false) { session.isValid }
        expect(true) { session != request.getSession(true) }
        session = request.getSession(true) as FakeHttpSession
        expect(true) { session.isValid }
        expect(null) { session.getAttribute("foo") }
    }

    @Test fun `getSession() creates a new session when invalidated`() {
        var session = request.session as FakeHttpSession
        expect(true) { session.isValid }
        session.setAttribute("foo", "bar")
        session.invalidate()
        expect(false) { session.isValid }
        expect(true) { session != request.session }
        session = request.session as FakeHttpSession
        expect(true) { session.isValid }
        expect(null) { session.getAttribute("foo") }
    }

    @Test fun `getSession() keeps a valid session`() {
        val session = request.session
        assertSame(session, request.getSession(true))
        assertSame(session, request.getSession(false))
    }

    @Test fun principal() {
        expect(null) { request.userPrincipal }
        expect(null) { request.remoteUser }
        request.userPrincipalInt = MockPrincipal("foo")
        expect(MockPrincipal("foo")) { request.userPrincipal }
        expect("foo") { request.remoteUser }
    }

    @Test fun logout() {
        request.userPrincipalInt = MockPrincipal("foo")
        request.logout()
        expect(null) { request.userPrincipal }
        expect(null) { request.remoteUser }
    }

    @Test fun isUserInRole() {
        expect(false) { request.isUserInRole("foo") }
        request.userPrincipalInt = MockPrincipal("foo")
        expect(false) { request.isUserInRole("foo") }
        request.userPrincipalInt = MockPrincipal("foo", listOf("foo"))
        expect(false) { request.isUserInRole("foo") }
        request.isUserInRole = { p, r -> (p as MockPrincipal).isUserInRole(r) }
        expect(true) { request.isUserInRole("foo") }
    }

    @Test fun changeSessionId() {
        val oldId = request.session.id
        val newId = request.changeSessionId()
        expect(false) { oldId == newId }
        expect(newId) { request.session.id }
    }

    @Test fun `inputStream fail when content not set`() {
        assertThrows<IllegalStateException> { request.inputStream }
    }

    @Test fun `inputStream provides correct content`() {
        request.content = "Foo".toByteArray()
        expect("Foo") { request.inputStream.readBytes().toString(Charsets.UTF_8) }
    }

    @Test fun `inputStream provides correct content 2`() {
        request.content = ByteArray(500_000) { it.toByte() }
        expect(request.content!!.toList()) { request.inputStream.readBytes().toList() }
    }

    @Test fun `multiple inputStream retrievals provide the same instance`() {
        request.content = "Foo".toByteArray()
        assertSame(request.inputStream, request.inputStream)
    }

    @Test fun `reader provides correct content`() {
        request.content = "Foo".toByteArray()
        expect("Foo") { request.reader.readText() }
    }

    @Test fun `reader decodes with the character encoding`() {
        request.content = "Foo".toByteArray(Charsets.UTF_16)
        request.setCharacterEncoding("UTF-16")
        expect("Foo") { request.reader.readText() }
    }

    @Test fun `multiple reader retrievals provides the same instance`() {
        request.content = "Foo".toByteArray()
        expect('F'.code) { request.reader.read() }
        expect('o'.code) { request.reader.read() }
    }

    @Test fun `reader provides empty content`() {
        request.content = ByteArray(0)
        expect("") { request.reader.readText() }
    }

    @Test fun `getInputStream() fails after getReader()`() {
        request.content = "Foo".toByteArray()
        request.reader
        assertThrows<IllegalStateException> { request.inputStream }
    }

    @Test fun `getReader() fails after getInputStream()`() {
        request.content = "Foo".toByteArray()
        request.inputStream
        assertThrows<IllegalStateException> { request.reader }
    }

    @Test fun `setting content starts over`() {
        request.content = "Foo".toByteArray()
        request.inputStream.read()
        request.content = "Bar".toByteArray()
        expect("Bar") { request.reader.readText() }
    }
}
