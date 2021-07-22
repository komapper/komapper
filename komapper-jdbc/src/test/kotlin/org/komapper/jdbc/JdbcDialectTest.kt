package org.komapper.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JdbcDialectTest {

    @Test
    fun extractJdbcDriver() {
        val driver = JdbcDialect.extractJdbcDriver("jdbc:aaa:bbb")
        assertEquals("aaa", driver)
    }

    @Test
    fun extractJdbcDriver_testcontainers() {
        val driver = JdbcDialect.extractJdbcDriver("jdbc:tc:aaa:bbb")
        assertEquals("aaa", driver)
    }

    @Test
    fun extractJdbcDriver_error() {
        assertThrows<IllegalStateException> {
            JdbcDialect.extractJdbcDriver("aaa:bbb")
        }
    }
}
