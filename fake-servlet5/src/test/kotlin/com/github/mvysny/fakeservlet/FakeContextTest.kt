package com.github.mvysny.fakeservlet

import jakarta.servlet.*
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.EventListener
import kotlin.test.assertIs
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

    @Test fun `realPath does not step out into a sibling folder sharing the root's name prefix`() {
        ctx.realPathRoots = listOf("src/test/web")
        expect(true) { File("src/test/webapp/VAADIN/themes/default/img/1.txt").exists() }
        expect(null) { ctx.getRealPath("/../webapp/VAADIN/themes/default/img/1.txt") }
    }

    @Nested inner class GetResource {
        @Test fun `from the real path`() {
            ctx.realPathRoots = listOf("src/test/webapp")
            val url = ctx.getResource("/VAADIN/themes/default/img/1.txt")!!
            expect("file") { url.protocol }
            expect(true) { url.path.endsWith("/src/test/webapp/VAADIN/themes/default/img/1.txt") }
            expect("") { ctx.getResourceAsStream("/VAADIN/themes/default/img/1.txt")!!.readBytes().decodeToString() }
        }

        @Test fun `from META-INF resources on the classpath`() {
            expect("from META-INF/resources") { ctx.getResourceAsStream("/webjar/foo.txt")!!.readBytes().decodeToString() }
        }

        @Test fun `VAADIN folder on the classpath`() {
            expect("from VAADIN") { ctx.getResourceAsStream("/VAADIN/themes/cp/foo.txt")!!.readBytes().decodeToString() }
            expect("from VAADIN") { ctx.getResourceAsStream("/VAADIN/themes/valo/../cp/foo.txt")!!.readBytes().decodeToString() }
        }

        @Test fun `VAADIN folder does not step out of VAADIN`() {
            expect(true) { Thread.currentThread().contextClassLoader.getResource("secret.txt") != null }
            expect(null) { ctx.getResource("/VAADIN/../secret.txt") }
        }

        @Test fun `missing resource`() {
            expect(null) { ctx.getResource("/nonexisting.txt") }
            expect(null) { ctx.getResource("/VAADIN/nonexisting.txt") }
            expect(null) { ctx.getResource("nonexisting.txt") }
            expect(null) { ctx.getResourceAsStream("/nonexisting.txt") }
        }
    }

    @Test fun serializable() {
        ctx.setAttribute("foo", "bar")
        ctx.setInitParameter("foo", "bar")
        ctx.cloneBySerialization()
    }

    @Test fun `serializable with registrations`() {
        ctx.addFilter("f", "com.example.FooFilter").addMappingForUrlPatterns(null, false, "/*")
        ctx.addServlet("s", "com.example.FooServlet").addMapping("/foo")
        ctx.sessionTimeout = 5
        val clone = ctx.cloneBySerialization()
        expectList("/*") { clone.getFilterRegistration("f")!!.urlPatternMappings.toList() }
        expectList("/foo") { clone.getServletRegistration("s")!!.mappings.toList() }
        expect(5) { clone.sessionTimeout }
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

    @Suppress("DEPRECATION")
    @Test fun defaults() {
        expect("") { ctx.contextPath }
        expect("Fake context") { ctx.servletContextName }
        expect("Mock") { ctx.serverInfo }
        expect("mock/localhost") { ctx.virtualServerName }
        expect(Thread.currentThread().contextClassLoader) { ctx.classLoader }
        expect(null) { ctx.jspConfigDescriptor }
        expect(null) { ctx.getServlet("foo") }
        expectList() { ctx.servlets.toList() }
        expectList() { ctx.servletNames.toList() }
        expect(setOf()) { ctx.getResourcePaths("/") }
        expect(30) { ctx.sessionTimeout }
        expect(null) { ctx.getRequestDispatcher("/foo") }
        expect(null) { ctx.getNamedDispatcher("foo") }
    }

    @Test fun getContext() {
        expect(ctx) { ctx.getContext("/") }
        expect(ctx) { ctx.getContext("/other") }
        expect(null) { ctx.getContext("other") }
    }

    @Test fun `create servlet, filter and listener`() {
        assertIs<TestServlet>(ctx.createServlet(TestServlet::class.java))
        assertIs<TestFilter>(ctx.createFilter(TestFilter::class.java))
        assertIs<TestListener>(ctx.createListener(TestListener::class.java))
        assertThrows<ServletException> { ctx.createServlet(NoDefaultConstructorServlet::class.java) }
        assertThrows<IllegalArgumentException> { ctx.createListener(NotAListener::class.java) }
    }

    @Test fun addListener() {
        expectList() { ctx.listeners }
        val listener = TestListener()
        ctx.addListener(listener)
        ctx.addListener(TestListener::class.java)
        ctx.addListener(TestListener::class.java.name)
        expect(3) { ctx.listeners.size }
        expect(listener) { ctx.listeners[0] }
        expect(true) { ctx.listeners.all { it is TestListener } }
        assertThrows<IllegalArgumentException> { ctx.addListener(NotAListener()) }
        assertThrows<ClassNotFoundException> { ctx.addListener("com.example.NoSuchListener") }
    }

    @Test fun declareRoles() {
        expect(setOf()) { ctx.declaredRoles }
        ctx.declareRoles("admin", "user")
        expect(setOf("admin", "user")) { ctx.declaredRoles }
        assertThrows<IllegalArgumentException> { ctx.declareRoles("") }
    }

    @Test fun `session timeout`() {
        ctx.sessionTimeout = 5
        expect(5) { ctx.sessionTimeout }
    }

    @Test fun `character encodings`() {
        expect(null) { ctx.requestCharacterEncoding }
        expect(null) { ctx.responseCharacterEncoding }
        ctx.requestCharacterEncoding = "UTF-8"
        ctx.responseCharacterEncoding = "UTF-16"
        expect("UTF-8") { ctx.requestCharacterEncoding }
        expect("UTF-16") { ctx.responseCharacterEncoding }
    }

    @Test fun `session cookie config`() {
        expect(true) { ctx.sessionCookieConfig === ctx.sessionCookieConfig }
        ctx.sessionCookieConfig.name = "JSESSIONID"
        expect("JSESSIONID") { ctx.sessionCookieConfig.name }
    }

    @Nested inner class SessionTrackingModes {
        @Test fun defaults() {
            expect(setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)) { ctx.defaultSessionTrackingModes }
            expect(setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)) { ctx.effectiveSessionTrackingModes }
        }

        @Test fun `set modes`() {
            ctx.setSessionTrackingModes(mutableSetOf(SessionTrackingMode.COOKIE))
            expect(setOf(SessionTrackingMode.COOKIE)) { ctx.effectiveSessionTrackingModes }
            ctx.setSessionTrackingModes(mutableSetOf(SessionTrackingMode.SSL))
            expect(setOf(SessionTrackingMode.SSL)) { ctx.effectiveSessionTrackingModes }
            // the default modes are unaffected
            expect(setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)) { ctx.defaultSessionTrackingModes }
        }

        @Test fun `SSL can not be combined with other modes`() {
            assertThrows<IllegalArgumentException> {
                ctx.setSessionTrackingModes(mutableSetOf(SessionTrackingMode.SSL, SessionTrackingMode.COOKIE))
            }
            expect(setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)) { ctx.effectiveSessionTrackingModes }
        }
    }

    @Test fun servletRegistrations() {
        expect(null) { ctx.getServletRegistration("foo") }
        expect(true) { ctx.servletRegistrations.isEmpty() }
        val reg = ctx.addServlet("foo", "com.example.FooServlet")
        expect(reg) { ctx.getServletRegistration("foo") }
        expect(mapOf<String, Any>("foo" to reg)) { ctx.servletRegistrations.toMap() }
    }

    @Test fun `addServlet() by class and by instance`() {
        val servlet = TestServlet()
        expect(TestServlet::class.java.name) { ctx.addServlet("byClass", TestServlet::class.java).className }
        expect(TestServlet::class.java.name) { ctx.addServlet("byInstance", servlet).className }
        expect(setOf("byClass", "byInstance")) { ctx.servletRegistrations.keys }
    }

    @Test fun `addJspFile() registers the servlet`() {
        val reg = ctx.addJspFile("jsp", "/index.jsp")
        expect("jsp") { reg.name }
        expect("/index.jsp") { reg.className }
        expect(reg) { ctx.getServletRegistration("jsp") }
    }

    @Test fun filterRegistrations() {
        expect(null) { ctx.getFilterRegistration("foo") }
        expect(true) { ctx.filterRegistrations.isEmpty() }
        val reg = ctx.addFilter("foo", "com.example.FooFilter")
        expect(reg) { ctx.getFilterRegistration("foo") }
        expect(mapOf<String, Any>("foo" to reg)) { ctx.filterRegistrations.toMap() }
        expect(mapOf("foo" to reg)) { ctx.filters.toMap() }
        // the returned map is a copy
        ctx.filterRegistrations.clear()
        expect(reg) { ctx.getFilterRegistration("foo") }
    }

    @Test fun `addFilter() by class and by instance`() {
        val filter = TestFilter()
        expect(TestFilter::class.java.name) { ctx.addFilter("byClass", TestFilter::class.java).className }
        expect(TestFilter::class.java.name) { ctx.addFilter("byInstance", filter).className }
        expect(setOf("byClass", "byInstance")) { ctx.filterRegistrations.keys }
    }

    @Test fun `log() does not throw`() {
        ctx.log("message")
        ctx.log("message", RuntimeException("expected"))
        @Suppress("DEPRECATION")
        ctx.log(RuntimeException("expected"), "message")
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

class TestServlet : GenericServlet() {
    override fun service(req: ServletRequest, res: ServletResponse) {}
}

class TestFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {}
}

class TestListener : HttpSessionListener {
    override fun sessionCreated(se: HttpSessionEvent) {}
}

class NotAListener : EventListener

class NoDefaultConstructorServlet(val name: String) : GenericServlet() {
    override fun service(req: ServletRequest, res: ServletResponse) {}
}
