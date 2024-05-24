package com.github.mvysny.fakeservlet

import jakarta.servlet.DispatcherType
import jakarta.servlet.FilterRegistration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

public class FakeFilterRegistration(public val filterName: String, public val filterClassName: String) : FilterRegistration.Dynamic {
    override fun getName(): String = filterName

    override fun getClassName(): String = filterClassName

    public val _initParameters: MutableMap<String, String> = ConcurrentHashMap<String, String>()

    override fun setInitParameter(name: String, value: String): Boolean = _initParameters.putIfAbsent(name, value) == null

    override fun getInitParameter(name: String): String? = _initParameters[name]

    override fun setInitParameters(initParameters: MutableMap<String, String>): MutableSet<String> {
        this._initParameters.putAll(initParameters)
        return mutableSetOf()
    }

    override fun getInitParameters(): Map<String, String> = Collections.unmodifiableMap(_initParameters)

    override fun addMappingForServletNames(
        dispatcherTypes: EnumSet<DispatcherType>,
        isMatchAfter: Boolean,
        vararg servletNames: String
    ) {
        TODO("Not yet implemented")
    }

    override fun getServletNameMappings(): MutableCollection<String> {
        TODO("Not yet implemented")
    }

    override fun addMappingForUrlPatterns(
        dispatcherTypes: EnumSet<DispatcherType>,
        isMatchAfter: Boolean,
        vararg urlPatterns: String
    ) {
        TODO("Not yet implemented")
    }

    override fun getUrlPatternMappings(): Collection<String> = listOf()

    public var _asyncSupported: Boolean = false

    override fun setAsyncSupported(isAsyncSupported: Boolean) {
        _asyncSupported = isAsyncSupported
    }
}