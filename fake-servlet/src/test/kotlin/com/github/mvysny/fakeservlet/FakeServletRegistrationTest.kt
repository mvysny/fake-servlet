package com.github.mvysny.fakeservlet

import org.junit.jupiter.api.Test
import kotlin.test.expect

class FakeServletRegistrationTest {
    private val reg: FakeServletRegistration = FakeServletRegistration("foo", "com.example.FooServlet")

    @Test fun mappings() {
        expectList() { reg.mappings.toList() }
        expect(setOf()) { reg.addMapping("/foo", "/bar") }
        expect(setOf()) { reg.addMapping("/foo") }
        expect(setOf("/foo", "/bar")) { reg.mappings.toSet() }
    }

    @Test fun runAsRole() {
        expect(null) { reg.runAsRole }
        reg.runAsRole = "admin"
        expect("admin") { reg.runAsRole }
    }

    @Test fun loadOnStartup() {
        expect(-1) { reg._loadOnStartup }
        reg.setLoadOnStartup(1)
        expect(1) { reg._loadOnStartup }
    }

    @Test fun serializable() {
        reg.addMapping("/foo")
        reg.runAsRole = "admin"
        val clone = reg.cloneBySerialization()
        expect(setOf("/foo")) { clone.mappings.toSet() }
        expect("admin") { clone.runAsRole }
    }
}
