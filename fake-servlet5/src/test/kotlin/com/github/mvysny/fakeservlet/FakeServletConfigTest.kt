package com.github.mvysny.fakeservlet

import org.junit.jupiter.api.Test
import kotlin.test.expect

class FakeServletConfigTest {
    private val ctx: FakeContext = FakeContext()
    private val config: FakeServletConfig = FakeServletConfig(ctx)

    @Test fun servletContext() {
        expect(ctx) { config.servletContext }
    }

    @Test fun servletName() {
        expect("Vaadin Servlet") { config.servletName }
    }

    @Test fun initParameters() {
        expect(null) { config.getInitParameter("foo") }
        expectList() { config.initParameterNames.toList() }
        config.servletInitParams["foo"] = "bar"
        expect("bar") { config.getInitParameter("foo") }
        expectList("foo") { config.initParameterNames.toList() }
    }
}
