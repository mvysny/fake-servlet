package com.github.mvysny.fakeservlet

import org.junit.jupiter.api.Test
import kotlin.test.expect

class FakeSessionCookieConfigTest {
    private val config: FakeSessionCookieConfig = FakeSessionCookieConfig()

    @Test fun defaults() {
        expect(null) { config.name }
        expect(null) { config.domain }
        expect(null) { config.path }
        expect(null) { config.comment }
        expect(false) { config.isHttpOnly }
        expect(false) { config.isSecure }
        expect(-1) { config.maxAge }
    }

    @Test fun setters() {
        config.name = "JSESSIONID"
        config.domain = "example.com"
        config.path = "/"
        config.comment = "session"
        config.isHttpOnly = true
        config.isSecure = true
        config.maxAge = 60
        expect(FakeSessionCookieConfig("JSESSIONID", "example.com", "/", "session", httpOnly = true, secure = true, maxAge = 60)) { config }
        expect("JSESSIONID") { config.name }
        expect("example.com") { config.domain }
        expect("/") { config.path }
        expect("session") { config.comment }
        expect(true) { config.isHttpOnly }
        expect(true) { config.isSecure }
        expect(60) { config.maxAge }
    }

    @Test fun serializable() {
        config.name = "JSESSIONID"
        expect(config) { config.cloneBySerialization() }
    }
}
