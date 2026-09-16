package com.github.mvysny.fakeservlet

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.AbstractMap
import kotlin.test.expect

class SessionAttributeMapTest {
    lateinit var session: FakeHttpSession
    lateinit var attrs: MutableMap<String, Any>
    @BeforeEach fun setup() {
        session = FakeHttpSession.create(FakeContext())
        attrs = session.attributes
    }

    @Nested inner class size {
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

    @Nested inner class `is empty`() {
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

    @Nested inner class remove {
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

    @Nested inner class Put {
        @Test fun `put returns the previous value`() {
            expect(null) { attrs.put("foo", "bar") }
            expect("bar") { attrs.put("foo", "baz") }
            expect("baz") { session.getAttribute("foo") }
        }
    }

    @Nested inner class Contains {
        @Test fun containsKey() {
            expect(false) { attrs.containsKey("foo") }
            session.setAttribute("foo", "bar")
            expect(true) { attrs.containsKey("foo") }
        }
        @Test fun containsValue() {
            expect(false) { attrs.containsValue("bar") }
            session.setAttribute("foo", "bar")
            expect(true) { attrs.containsValue("bar") }
        }
    }

    @Nested inner class `remove key and value` {
        @Test fun `removes when the value matches`() {
            attrs["foo"] = "bar"
            expect(true) { attrs.remove("foo", "bar") }
            expect(null) { session.getAttribute("foo") }
        }
        @Test fun `keeps when the value differs`() {
            attrs["foo"] = "bar"
            expect(false) { attrs.remove("foo", "baz") }
            expect("bar") { session.getAttribute("foo") }
        }
    }

    @Nested inner class Views {
        @Test fun `keys, values and entries`() {
            session.setAttribute("foo", "bar")
            session.setAttribute("baz", "qux")
            expect(setOf("foo", "baz")) { attrs.keys.toSet() }
            expect(setOf("bar", "qux")) { attrs.values.toSet() }
            expect(mapOf("foo" to "bar", "baz" to "qux")) { attrs.entries.associate { it.key to it.value } }
            expect(mapOf("foo" to "bar", "baz" to "qux")) { attrs.toMap() }
        }
        @Test fun `entries size`() {
            expect(0) { attrs.entries.size }
            session.setAttribute("foo", "bar")
            expect(1) { attrs.entries.size }
        }
        @Test fun `entries add`() {
            expect(true) { attrs.entries.add(AbstractMap.SimpleEntry("foo", "bar")) }
            expect("bar") { session.getAttribute("foo") }
            expect(false) { attrs.entries.add(AbstractMap.SimpleEntry("foo", "bar")) }
        }
        @Test fun `entries contains and remove`() {
            session.setAttribute("foo", "bar")
            expect(true) { attrs.entries.contains(AbstractMap.SimpleEntry("foo", "bar")) }
            expect(false) { attrs.entries.contains(AbstractMap.SimpleEntry("foo", "baz")) }
            expect(false) { attrs.entries.remove(AbstractMap.SimpleEntry("foo", "baz")) }
            expect("bar") { session.getAttribute("foo") }
            expect(true) { attrs.entries.remove(AbstractMap.SimpleEntry("foo", "bar")) }
            expect(null) { session.getAttribute("foo") }
        }
        @Test fun `keys remove`() {
            session.setAttribute("foo", "bar")
            expect(true) { attrs.keys.remove("foo") }
            expect(null) { session.getAttribute("foo") }
        }
        @Test fun `iterator remove`() {
            (0..10).forEach { session.setAttribute(it.toString(), it) }
            val iterator = attrs.entries.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().key.toInt() % 2 == 0) iterator.remove()
            }
            expect(setOf("1", "3", "5", "7", "9")) { attrs.keys.toSet() }
        }
        @Test fun `iterator remove without next fails`() {
            session.setAttribute("foo", "bar")
            val iterator = attrs.entries.iterator()
            assertThrows<IllegalStateException> { iterator.remove() }
            iterator.next()
            iterator.remove()
            assertThrows<IllegalStateException> { iterator.remove() }
        }
        @Test fun `iterator next past the end fails`() {
            assertThrows<NoSuchElementException> { attrs.entries.iterator().next() }
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
