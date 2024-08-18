@file:Suppress("DEPRECATION")

package com.github.mvysny.fakeservlet

import java.io.Serializable
import jakarta.servlet.http.HttpSession
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

    @Test fun serializable() {
        session.setAttribute("foo", "bar")
        (session as Serializable).cloneBySerialization()
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
        @Test fun `getAttribute() succeeds on invalidated session`() {
            session.invalidate()
            assertThrows<IllegalStateException> {
                session.getAttribute("foo")
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
