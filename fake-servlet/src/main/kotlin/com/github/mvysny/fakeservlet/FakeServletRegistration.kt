package com.github.mvysny.fakeservlet

import java.io.Serializable
import java.util.concurrent.CopyOnWriteArraySet
import javax.servlet.MultipartConfigElement
import javax.servlet.ServletRegistration
import javax.servlet.ServletSecurityElement

public class FakeServletRegistration(name: String, className: String) : FakeRegistration(name, className), ServletRegistration.Dynamic, Serializable {
    private val _urlPatterns = CopyOnWriteArraySet<String>()

    override fun addMapping(vararg urlPatterns: String): Set<String> {
        _urlPatterns.addAll(urlPatterns)
        return setOf()
    }

    override fun getMappings(): Collection<String> = _urlPatterns.toSet()

    public var _runAsRole: String? = null

    override fun getRunAsRole(): String? = _runAsRole

    public var _loadOnStartup: Int = -1

    override fun setLoadOnStartup(loadOnStartup: Int) {
        this._loadOnStartup = loadOnStartup
    }

    override fun setServletSecurity(constraint: ServletSecurityElement): MutableSet<String> {
        TODO("Not yet implemented")
    }

    override fun setMultipartConfig(multipartConfig: MultipartConfigElement?) {
        TODO("Not yet implemented")
    }

    override fun setRunAsRole(roleName: String?) {
        this._runAsRole = roleName
    }
}
