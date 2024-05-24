package com.github.mvysny.fakeservlet

import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.DispatcherType
import javax.servlet.FilterRegistration
import javax.servlet.Registration

public open class FakeRegistration(
    private val name: String,
    private val className: String
) : Registration.Dynamic, Serializable {
    override final fun getName(): String = name
    override final fun getClassName(): String = className

    public val _initParameters: MutableMap<String, String> = ConcurrentHashMap<String, String>()

    override final fun setInitParameter(name: String, value: String): Boolean = _initParameters.putIfAbsent(name, value) == null

    override final fun getInitParameter(name: String): String? = _initParameters[name]

    override final fun setInitParameters(initParameters: MutableMap<String, String>): MutableSet<String> {
        val result = mutableSetOf<String>()
        initParameters.forEach { (key, value) -> if (!setInitParameter(key, value)) result.add(value) }
        return result
    }

    override final fun getInitParameters(): Map<String, String> = Collections.unmodifiableMap(_initParameters)

    public var _asyncSupported: Boolean = false

    override final fun setAsyncSupported(isAsyncSupported: Boolean) {
        _asyncSupported = isAsyncSupported
    }

    override fun toString(): String =
        "${javaClass.simpleName}(name='$name', className='$className', initParameters=$_initParameters, _asyncSupported=$_asyncSupported)"
}

public class FakeFilterRegistration(name: String, className: String) : FakeRegistration(name, className), FilterRegistration.Dynamic, Serializable {
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
}
