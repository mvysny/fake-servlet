package com.github.mvysny.kaributesting.mockhttp

import com.github.mvysny.fakeservlet.attributes
import javax.servlet.http.HttpSession

@Deprecated("renamed")
public val HttpSession.attributes: MutableMap<String, Any>
    get() = attributes
