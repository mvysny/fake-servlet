package com.github.mvysny.fakeservlet

import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletResponse

public open class FakeServletConfig(public val context: ServletContext) : ServletConfig {

    /**
     * Per-servlet init parameters.
     */
    public var servletInitParams: MutableMap<String, String> = mutableMapOf<String, String>()

    override fun getInitParameter(name: String): String? = servletInitParams[name]

    override fun getInitParameterNames(): Enumeration<String> = Collections.enumeration(servletInitParams.keys)

    override fun getServletName(): String = "Vaadin Servlet"

    override fun getServletContext(): ServletContext = context
}

internal fun <K, V> MutableMap<K, V>.putOrRemove(key: K, value: V?) {
    if (value == null) remove(key) else set(key, value)
}

public object FakeHttpEnvironment {
    /**
     * [FakeRequest.getLocalPort]
     */
    public var localPort: Int = 8080

    /**
     * [FakeRequest.getServerPort]
     */
    public var serverPort: Int = 8080

    /**
     * [FakeRequest.getRemotePort]
     */
    public var remotePort: Int = 8080

    /**
     * [FakeRequest.getRemoteAddr]
     */
    public var remoteAddr: String = "127.0.0.1"

    /**
     * [FakeRequest.getAuthType]
     */
    public var authType: String? = null

    /**
     * [FakeRequest.isSecure]
     */
    public var isSecure: Boolean = false

    /**
     * [FakeRequest.authenticate]
     */
    public var authenticator: (HttpServletResponse) -> Boolean = { throw UnsupportedOperationException("not implemented. Set MockHttpEnvironment.authenticator to override") }

    /**
     * According to the servlet spec, HttpSession functions should fail on an invalidated session.
     * However, apparently the servlet containers do not take this check too seriously since existing Spring Security servlet-related
     * code apparently works and is happily reading and writing http session attributes on an invalidated session.
     *
     * Therefore this setting defaults to false which disables all session invalidity checks. Turn this
     * to true, to make [FakeHttpSession] behave according to the spec.
     *
     * See [FakeHttpSession.getAttribute] implementation for more details.
     */
    public var strictSessionValidityChecks: Boolean = false
}
