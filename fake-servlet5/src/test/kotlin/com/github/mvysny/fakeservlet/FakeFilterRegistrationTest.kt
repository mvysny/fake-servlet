package com.github.mvysny.fakeservlet

import jakarta.servlet.DispatcherType
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.expect

class FakeFilterRegistrationTest {
    private val reg: FakeFilterRegistration = FakeFilterRegistration("foo", "com.example.FooFilter")

    @Test fun nameAndClassName() {
        expect("foo") { reg.name }
        expect("com.example.FooFilter") { reg.className }
    }

    @Test fun initParameters() {
        expect(null) { reg.getInitParameter("a") }
        expect(mapOf()) { reg.initParameters }
        expect(true) { reg.setInitParameter("a", "1") }
        expect(false) { reg.setInitParameter("a", "2") }
        expect("1") { reg.getInitParameter("a") }
        expect(mapOf("a" to "1")) { reg.initParameters }
    }

    @Test fun `setInitParameters() sets all parameters when there is no conflict`() {
        expect(setOf()) { reg.setInitParameters(mutableMapOf("a" to "1", "b" to "2")) }
        expect(mapOf("a" to "1", "b" to "2")) { reg.initParameters }
    }

    @Test fun `setInitParameters() returns the conflicting names and sets nothing`() {
        reg.setInitParameter("a", "1")
        expect(setOf("a")) { reg.setInitParameters(mutableMapOf("a" to "2", "b" to "3")) }
        expect(mapOf("a" to "1")) { reg.initParameters }
    }

    @Test fun asyncSupported() {
        expect(false) { reg._asyncSupported }
        reg.setAsyncSupported(true)
        expect(true) { reg._asyncSupported }
    }

    @Test fun servletNameMappings() {
        expectList() { reg.servletNameMappings.toList() }
        reg.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST), true, "s1", "s2")
        reg.addMappingForServletNames(null, false, "s1")
        expect(setOf("s1", "s2")) { reg.servletNameMappings.toSet() }
    }

    @Test fun urlPatternMappings() {
        expectList() { reg.urlPatternMappings.toList() }
        reg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*", "/foo")
        reg.addMappingForUrlPatterns(null, false, "/bar")
        expect(setOf("/*", "/foo", "/bar")) { reg.urlPatternMappings.toSet() }
    }

    @Test fun `toString()`() {
        reg.setInitParameter("a", "1")
        expect("FakeFilterRegistration(name='foo', className='com.example.FooFilter', initParameters={a=1}, _asyncSupported=false)") { reg.toString() }
    }

    @Test fun serializable() {
        reg.setInitParameter("a", "1")
        reg.addMappingForServletNames(null, false, "s1")
        val clone = reg.cloneBySerialization()
        expect(mapOf("a" to "1")) { clone.initParameters }
        expect(setOf("s1")) { clone.servletNameMappings.toSet() }
    }
}
