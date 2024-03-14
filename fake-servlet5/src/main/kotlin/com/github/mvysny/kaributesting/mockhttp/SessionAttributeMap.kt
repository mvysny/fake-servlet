package com.github.mvysny.kaributesting.mockhttp

import com.github.mvysny.fakeservlet.attributes
import jakarta.servlet.http.HttpSession

@Deprecated("renamed")
public val HttpSession.attributes: MutableMap<String, Any>
    get() = attributes
