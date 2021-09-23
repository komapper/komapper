package org.komapper.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
        assertThrows<IllegalStateException> {
            JdbcDialects.extractJdbcDriver("aaa:bbb")
        }
    }
}
