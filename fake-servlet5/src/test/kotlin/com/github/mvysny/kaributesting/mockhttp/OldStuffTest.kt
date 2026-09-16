@file:Suppress("DEPRECATION")

package com.github.mvysny.kaributesting.mockhttp

import org.junit.jupiter.api.Test
import kotlin.test.expect

class OldStuffTest {
    @Test fun `deprecated aliases still compile and work`() {
        val ctx = MockContext()
        val session = MockHttpSession.create(ctx)
        val request = MockRequest(session)
        MockResponse().setHeader("foo", "bar")
        expect(ctx) { MockServletConfig(ctx).servletContext }
        expect(false) { MockHttpEnvironment.strictSessionValidityChecks }
        expect(session) { request.session }
    }

    @Test fun `deprecated attributes`() {
        val session = MockHttpSession.create(MockContext())
        session.attributes["foo"] = "bar"
        expect("bar") { session.getAttribute("foo") }
    }
}
