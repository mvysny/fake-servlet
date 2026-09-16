@file:Suppress("DEPRECATION")

package com.github.mvysny.fakeservlet

import java.io.Serializable
import javax.servlet.http.HttpSession
import org.junit.jupiter.api.*
import kotlin.test.expect

class FakeHttpSessionTest {
    private lateinit var session: HttpSession
    @BeforeEach fun setup() {
        session = FakeHttpSession.create(FakeContext())
    }

    @Test fun attributes() {
        expect(null) { session.getAttribute("foo") }
        expectList() { session.attributeNames.toList() }
        session.setAttribute("foo", "bar")
        expectList("foo") { session.attributeNames.toList() }
        expect("bar") { session.getAttribute("foo") }
        session.setAttribute("foo", null)
        expect(null) { session.getAttribute("foo") }
        expectList() { session.attributeNames.toList() }
        session.setAttribute("foo", "bar")
        expect("bar") { session.getAttribute("foo") }
        expectList("foo") { session.attributeNames.toList() }
        session.removeAttribute("foo")
        expect(null) { session.getAttribute("foo") }
        expectList() { session.attributeNames.toList() }
    }

    @Test fun `deprecated value functions delegate to attributes`() {
        expect(null) { session.getValue("foo") }
        session.putValue("foo", "bar")
        expect("bar") { session.getAttribute("foo") }
        expect("bar") { session.getValue("foo") }
        expectList("foo") { session.valueNames.toList() }
        session.removeValue("foo")
        expect(null) { session.getAttribute("foo") }
        expectList() { session.valueNames.toList() }
    }

    @Test fun serializable() {
        session.setAttribute("foo", "bar")
        val clone = (session as Serializable).cloneBySerialization() as HttpSession
        expect("bar") { clone.getAttribute("foo") }
        expect(session.id) { clone.id }
    }

    @Test fun `maxInactiveInterval is the context session timeout, in seconds`() {
        expect(30 * 60) { session.maxInactiveInterval }
        val ctx = FakeContext()
        ctx.sessionTimeout = 5
        expect(5 * 60) { FakeHttpSession.create(ctx).maxInactiveInterval }
    }

    @Test fun `set maxInactiveInterval`() {
        session.maxInactiveInterval = 10
        expect(10) { session.maxInactiveInterval }
    }

    @Test fun lastAccessedTime() {
        expect(session.creationTime) { session.lastAccessedTime }
    }

    @Test fun defaults() {
        expect(false) { session.isNew }
        expect(null) { session.sessionContext }
        expect(true) { session.servletContext is FakeContext }
        expect(true) { (session as FakeHttpSession).isValid }
    }

    @Test fun `create() generates unique ids`() {
        expect(false) { session.id == FakeHttpSession.create(FakeContext()).id }
    }

    @Test fun `copy constructor keeps id, context, creationTime, timeout and attributes`() {
        session.setAttribute("foo", "bar")
        session.maxInactiveInterval = 10
        val copy = FakeHttpSession(session)
        expect(session.id) { copy.id }
        expect(session.servletContext) { copy.servletContext }
        expect(session.creationTime) { copy.creationTime }
        expect(10) { copy.maxInactiveInterval }
        expect("bar") { copy.getAttribute("foo") }
        // the attributes are copied, not shared
        copy.setAttribute("foo", "baz")
        expect("bar") { session.getAttribute("foo") }
    }

    @Test fun copyAttributes() {
        session.setAttribute("foo", "bar")
        val other = FakeHttpSession.create(FakeContext())
        other.setAttribute("baz", "qux")
        expect(other) { other.copyAttributes(session) }
        expect(setOf("foo", "baz")) { other.attributeNames.toList().toSet() }
    }

    @Test fun destroy() {
        session.setAttribute("foo", "bar")
        (session as FakeHttpSession).destroy()
        expectList() { session.attributeNames.toList() }
    }

    @Test fun changeSessionId() {
        val oldId = session.id
        val newId = (session as FakeHttpSession).changeSessionId()
        expect(false) { oldId == newId }
        expect(newId) { session.id }
    }

    @Test fun `toString()`() {
        session.setAttribute("foo", "bar")
        expect(true) { session.toString().contains("sessionId='${session.id}'") }
        expect(true) { session.toString().contains("attributes={foo=bar}") }
    }

    @Nested inner class `invalidate without strict checks` {
        @Test fun `session keeps working`() {
            session.setAttribute("foo", "bar")
            session.invalidate()
            expect(false) { (session as FakeHttpSession).isValid }
            expect("bar") { session.getAttribute("foo") }
            session.setAttribute("foo", "baz")
            expect("baz") { session.getAttribute("foo") }
            session.creationTime
        }
        @Test fun `calling invalidate() second time succeeds`() {
            session.invalidate()
            session.invalidate()
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested inner class invalidate {
        @BeforeAll fun setStrictValidityChecks() {
            FakeHttpEnvironment.strictSessionValidityChecks = true
        }
        @AfterAll fun revertStrictValidityChecks() {
            FakeHttpEnvironment.strictSessionValidityChecks = false
        }

        @Test fun smoke() {
            session.invalidate()
            expect(false) { (session as FakeHttpSession).isValid }
        }
        @Test fun `calling invalidate() second time throws`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.invalidate()
            }
        }
        @Test fun `getAttribute() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.getAttribute("foo")
            }
        }
        @Test fun `setAttribute() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.setAttribute("foo", "bar")
            }
        }
        @Test fun `removeAttribute() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.removeAttribute("foo")
            }
        }
        @Test fun `isNew() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.isNew
            }
        }
        @Test fun `getId() succeeds on invalidated session`() {
            session.invalidate()
            session.id
        }
        @Test fun `getServletContext() succeeds on invalidated session`() {
            session.invalidate()
            session.servletContext
        }
        @Test fun `maxActiveInterval succeeds on invalidated session`() {
            session.invalidate()
            session.maxInactiveInterval = session.maxInactiveInterval + 1
        }
        @Test fun `getCreationTime() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.creationTime
            }
        }
        @Test fun `getLastAccessedTime() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.lastAccessedTime
            }
        }
        @Test fun `getAttributeNames() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.attributeNames
            }
        }
        @Test fun `getValueNames() fails on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.valueNames
            }
        }
    }
}
