package com.github.mvysny.fakeservlet

import com.github.mvysny.dynatest.DynaTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * @author mavi
 */
class SessionAttributeMapTest {
    private lateinit var session: FakeHttpSession
    private lateinit var attrs: MutableMap<String, Any>
    @BeforeEach fun setup() {
        session = FakeHttpSession.create(FakeContext())
        attrs = session.attributes
    }

    @Nested inner class size() {
        @Test fun `Initially zero`() {
            expect(0) { attrs.size }
        }
        @Test fun `put increases by 1`() {
            attrs["foo"] = "bar"
            expect(1) { attrs.size }
        }
        @Test fun `setAttribute increases by 1`() {
            session.setAttribute("foo", "bar")
            expect(1) { attrs.size }
        }
        @Test fun `clear sets size to 0`() {
            attrs["foo"] = "bar"
            attrs.clear()
            expect(0) { attrs.size }
        }
    }

    @Nested inner class IsEmpty {
        @Test fun `Initially true`() {
            expect(true) { attrs.isEmpty() }
        }
        @Test fun `put makes map not empty`() {
            attrs["foo"] = "bar"
            expect(false) { attrs.isEmpty() }
        }
        @Test fun `setAttribute makes map not empty`() {
            session.setAttribute("foo", "bar")
            expect(false) { attrs.isEmpty() }
        }
        @Test fun `clear sets size to 0`() {
            attrs["foo"] = "bar"
            attrs.clear()
            expect(true) { attrs.isEmpty() }
        }
    }

    @Nested inner class Get {
        @Test fun `get from empty map returns null`() {
            expect(null) { attrs["foo"] }
        }
        @Test fun `get non-existing key returns null`() {
            session.setAttribute("foo", "bar")
            expect(null) { attrs["bar"] }
        }
        @Test fun `existing key retrieval`() {
            session.setAttribute("foo", "bar")
            expect("bar") { attrs["foo"] }
        }
        @Test fun `get deleted key returns null`() {
            session.setAttribute("foo", "bar")
            attrs.remove("foo")
            expect(null) { attrs["foo"] }
        }
    }

    @Nested inner class Remove {
        @Test fun `remove from empty map does nothing`() {
            expect(null) { attrs.remove("foo") }
            expect(true) { attrs.isEmpty() }
        }
        @Test fun `remove non-existing key does nothing`() {
            attrs["bar"] = "foo"
            expect(null) { attrs.remove("foo") }
            expect("foo") { attrs["bar"] }
            expect("foo") { session.getAttribute("bar") }
        }
        @Test fun `remove existing key`() {
            attrs["bar"] = "foo"
            expect("foo") { attrs.remove("bar") }
            expect(true) { attrs.isEmpty() }
            expect(null) { session.getAttribute("bar") }
        }
    }

    @Nested inner class clear {
        @Test fun `clear empty map does nothing`() {
            attrs.clear()
            expect(true) { attrs.isEmpty() }
        }
        @Test fun `clear map with one key`() {
            attrs["foo"] = "bar"
            attrs.clear()
            expect(true) { attrs.isEmpty() }
        }
        @Test fun `clear big map`() {
            (0..1000).forEach { session.setAttribute(it.toString(), it) }
            expect(false) { attrs.isEmpty() }
            attrs.clear()
            expect(true) { attrs.isEmpty() }
            (0..1000).forEach {
                expect(null) { session.getAttribute(it.toString()) }
            }
        }
    }
}
