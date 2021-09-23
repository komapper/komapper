package org.komapper.r2dbc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class R2DbcDialectsTest {

    @Test
    fun extractR2dbcDriver() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:h2:mem:///testdb")
        Assertions.assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_ssl() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbcs:h2:mem:///testdb")
        Assertions.assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_testcontainers() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:tc:h2:mem:///testdb")
        Assertions.assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_error() {
        assertThrows<IllegalStateException> {
            R2dbcDialects.extractR2dbcDriver("jdbc:h2:mem:///testdb")
        }
    }
}
