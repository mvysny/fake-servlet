package com.github.mvysny.fakeservlet

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.DispatcherType
import javax.servlet.FilterRegistration

public class FakeFilterRegistration(public val filterName: String, public val filterClassName: String) : FilterRegistration.Dynamic {
    override fun getName(): String = filterName

    override fun getClassName(): String = filterClassName

    public val _initParameters: MutableMap<String, String> = ConcurrentHashMap<String, String>()

    override fun setInitParameter(name: String, value: String): Boolean = _initParameters.putIfAbsent(name, value) == null

    override fun getInitParameter(name: String): String? = _initParameters[name]

    override fun setInitParameters(initParameters: MutableMap<String, String>): MutableSet<String> {
        val result = mutableSetOf<String>()
        initParameters.forEach { (key, value) -> if (!setInitParameter(key, value)) result.add(value) }
        return result
    }

    override fun getInitParameters(): Map<String, String> = Collections.unmodifiableMap(_initParameters)

    public val _servletNameMappings: MutableMap<String, String> = ConcurrentHashMap<String, String>()

    override fun addMappingForServletNames(
        dispatcherTypes: EnumSet<DispatcherType>,
        isMatchAfter: Boolean,
        vararg servletNames: String
    ) {
        servletNames.forEach { _servletNameMappings[it] = it }
    }

    override fun getServletNameMappings(): MutableCollection<String> = Collections.unmodifiableSet(_servletNameMappings.keys)

    public val _urlPatternMappings: MutableMap<String, String> = ConcurrentHashMap<String, String>()

    override fun addMappingForUrlPatterns(
        dispatcherTypes: EnumSet<DispatcherType>,
        isMatchAfter: Boolean,
        vararg urlPatterns: String
    ) {
        urlPatterns.forEach { _urlPatternMappings[it] = it }
    }

    override fun getUrlPatternMappings(): Collection<String> = Collections.unmodifiableSet(_urlPatternMappings.keys)

    public var _asyncSupported: Boolean = false

    override fun setAsyncSupported(isAsyncSupported: Boolean) {
        _asyncSupported = isAsyncSupported
    }
}