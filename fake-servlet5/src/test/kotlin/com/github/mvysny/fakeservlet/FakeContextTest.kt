package com.github.mvysny.fakeservlet

import jakarta.servlet.MultipartConfigElement
import jakarta.servlet.ServletSecurityElement
import org.junit.jupiter.api.Test
import kotlin.test.expect

class FakeContextTest {
    private val ctx: FakeContext = FakeContext()

    @Test fun attributes() {
        expect(null) { ctx.getAttribute("foo") }
        expectList() { ctx.attributeNames.toList() }
        ctx.setAttribute("foo", "bar")
        expect("bar") { ctx.getAttribute("foo") }
        expectList("foo") { ctx.attributeNames.toList() }
        ctx.setAttribute("foo", null)
        expect(null) { ctx.getAttribute("foo") }
        expectList() { ctx.attributeNames.toList() }
        ctx.setAttribute("foo", "bar")
        expectList("foo") { ctx.attributeNames.toList() }
        expect("bar") { ctx.getAttribute("foo") }
        ctx.removeAttribute("foo")
        expectList() { ctx.attributeNames.toList() }
        expect(null) { ctx.getAttribute("foo") }
    }

    @Test fun initParameters() {
        expect(null) { ctx.getInitParameter("foo") }
        expectList() { ctx.initParameterNames.toList() }
        expect(true) { ctx.setInitParameter("foo", "bar") }
        expectList("foo") { ctx.initParameterNames.toList() }
        expect("bar") { ctx.getInitParameter("foo") }
        expect(false) { ctx.setInitParameter("foo", "baz") }
        expect("bar") { ctx.getInitParameter("foo") }
        expectList("foo") { ctx.initParameterNames.toList() }
    }

    @Test fun realPath() {
        ctx.realPathRoots = listOf("src/main/webapp/frontend", "src/main/webapp", "src/test/webapp")
        expect(null) { ctx.getRealPath("/index.html") }
        expect(true) { ctx.getRealPath("/VAADIN/themes/default/img/1.txt")!!.replace('\\', '/').endsWith("/VAADIN/themes/default/img/1.txt") }
        expect(true) { ctx.getRealPath("/VAADIN/themes/valo/../default/img/1.txt")!!.replace('\\', '/').endsWith("/VAADIN/themes/default/img/1.txt") }
        // stepping out of root is not allowed and returns null. Avoids browsing through the filesystem
        expect(null) { ctx.getRealPath("/../../../build.gradle.kts") }
    }

    @Test fun serializable() {
        ctx.setAttribute("foo", "bar")
        ctx.setInitParameter("foo", "bar")
        ctx.cloneBySerialization()
    }

    @Test fun version() {
        expect(5) { ctx.majorVersion }
        expect(0) { ctx.minorVersion }
        expect(5) { ctx.effectiveMajorVersion }
        expect(0) { ctx.effectiveMinorVersion }
    }

    @Test fun mimeType() {
        expect("text/html") { ctx.getMimeType("index.html") }
        expect(null) { ctx.getMimeType("foo.unknownextension") }
    }

    @Test fun servletRegistrations() {
        expect(null) { ctx.getServletRegistration("foo") }
        expect(true) { ctx.servletRegistrations.isEmpty() }
        val reg = ctx.addServlet("foo", "com.example.FooServlet")
        expect(reg) { ctx.getServletRegistration("foo") }
        expect(mapOf<String, Any>("foo" to reg)) { ctx.servletRegistrations.toMap() }
    }

    @Test fun `setInitParameters() returns the conflicting names`() {
        val reg = ctx.addFilter("foo", "com.example.FooFilter")
        reg.setInitParameter("a", "1")
        expect(setOf("a")) { reg.setInitParameters(mutableMapOf("a" to "2", "b" to "3")) }
        expect(mapOf("a" to "1", "b" to "3")) { reg.initParameters }
    }

    @Test fun `servlet security and multipart config`() {
        val reg = ctx.addServlet("foo", "com.example.FooServlet") as FakeServletRegistration
        val security = ServletSecurityElement()
        expect(setOf()) { reg.setServletSecurity(security) }
        expect(security) { reg._servletSecurity }
        val multipart = MultipartConfigElement("/tmp")
        reg.setMultipartConfig(multipart)
        expect(multipart) { reg._multipartConfig }
        ctx.cloneBySerialization()
    }
}
