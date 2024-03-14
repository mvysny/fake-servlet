package com.github.mvysny.fakeservlet

import java.security.Principal

data class MockPrincipal(private val name: String, val allowedRoles: List<String> = listOf()) : Principal {
    override fun getName(): String = name

    fun isUserInRole(role: String): Boolean = allowedRoles.contains(role)
}
