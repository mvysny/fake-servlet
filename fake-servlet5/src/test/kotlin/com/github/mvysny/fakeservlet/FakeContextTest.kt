package com.github.mvysny.fakeservlet

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
}
