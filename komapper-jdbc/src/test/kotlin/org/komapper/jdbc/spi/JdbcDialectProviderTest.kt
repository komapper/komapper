package org.komapper.jdbc.spi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JdbcDialectProviderTest {

    @Test
    fun extractJdbcDriver() {
        val driver = JdbcDialectProvider.extractJdbcDriver("jdbc:aaa:bbb")
        assertEquals("aaa", driver)
    }

    @Test
    fun extractJdbcDriver_testcontainers() {
        val driver = JdbcDialectProvider.extractJdbcDriver("jdbc:tc:aaa:bbb")
        assertEquals("aaa", driver)
    }

    @Test
    fun extractJdbcDriver_error() {
        assertThrows<IllegalStateException> {
            JdbcDialectProvider.extractJdbcDriver("aaa:bbb")
        }
    }
}
