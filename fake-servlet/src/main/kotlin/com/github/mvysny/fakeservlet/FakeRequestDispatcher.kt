package com.github.mvysny.fakeservlet

import javax.servlet.RequestDispatcher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletResponseWrapper

/**
 * Records a dispatch on the [FakeResponse] instead of running the target, which the fake container can't:
 *
 * ```
 * request.getRequestDispatcher("/login.jsp").forward(request, response)
 * expect("/login.jsp") { response.forwardedUrl }
 * ```
 *
 * A response that isn't a [FakeResponse], even after unwrapping [ServletResponseWrapper]s, records nothing.
 *
 * @property target the absolute path, or the servlet name for [FakeContext.getNamedDispatcher].
 */
public open class FakeRequestDispatcher(public val target: String) : RequestDispatcher {
    /**
     * Clears the buffer, sets [FakeResponse.forwardedUrl] and commits the response, as a real forward would.
     * @throws IllegalStateException if the response is already committed.
     */
    override fun forward(request: ServletRequest, response: ServletResponse) {
        response.resetBuffer()
        val fake = response.unwrap() ?: return
        fake.forwardedUrl = target
        fake._committed = true
    }

    /**
     * Appends [target] to [FakeResponse.includedUrls].
     */
    override fun include(request: ServletRequest, response: ServletResponse) {
        response.unwrap()?.includedUrls?.add(target)
    }

    private fun ServletResponse.unwrap(): FakeResponse? = when (this) {
        is FakeResponse -> this
        is ServletResponseWrapper -> response.unwrap()
        else -> null
    }
}
