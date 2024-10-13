package org.komapper.jdbc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JdbcDialectsTest {
    @Test
    fun extractJdbcDriver() {
        val driver = JdbcDialects.extractJdbcDriver("jdbc:aaa:bbb")
        assertEquals("aaa", driver)
    }

    @Test
    fun extractJdbcDriver_testcontainers() {
        val driver = JdbcDialects.extractJdbcDriver("jdbc:tc:aaa:bbb")
        assertEquals("aaa", driver)
    }

    @Test
    fun extractJdbcDriver_error() {
        assertFailsWith<IllegalStateException> {
            JdbcDialects.extractJdbcDriver("aaa:bbb")
        }
    }
}
