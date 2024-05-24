package com.github.mvysny.fakeservlet

import jakarta.servlet.SessionCookieConfig
import java.io.Serializable

public data class FakeSessionCookieConfig(
    private var name: String? = null,
    private var domain: String? = null,
    private var path: String? = null,
    private var comment: String? = null,
    private var httpOnly: Boolean = false,
    private var secure: Boolean = false,
    private var maxAge: Int = -1,
) : SessionCookieConfig, Serializable {

    override fun setName(name: String?) {
        this.name = name
    }

    override fun getName(): String? = name

    override fun setDomain(domain: String?) {
        this.domain = domain
    }

    override fun getDomain(): String? = domain

    override fun setPath(path: String?) {
        this.path = path
    }

    override fun getPath(): String? = path

    override fun setComment(comment: String?) {
        this.comment = comment
    }

    override fun getComment(): String? = comment

    override fun setHttpOnly(httpOnly: Boolean) {
        this.httpOnly = httpOnly
    }

    override fun isHttpOnly(): Boolean = httpOnly

    override fun setSecure(secure: Boolean) {
        this.secure = secure
    }

    override fun isSecure(): Boolean = secure

    override fun setMaxAge(maxAge: Int) {
        this.maxAge = maxAge
    }

    override fun getMaxAge(): Int = maxAge
}