package org.komapper.r2dbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class R2dbcDialectTest {

    @Test
    fun extractR2dbcDriver() {
        val driver = R2dbcDialect.extractR2dbcDriver("r2dbc:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_ssl() {
        val driver = R2dbcDialect.extractR2dbcDriver("r2dbcs:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_error() {
        assertThrows<IllegalStateException> {
            R2dbcDialect.extractR2dbcDriver("jdbc:h2:mem:///testdb")
        }
    }
}
