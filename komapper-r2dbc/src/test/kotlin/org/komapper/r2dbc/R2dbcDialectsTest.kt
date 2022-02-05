package org.komapper.r2dbc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class R2dbcDialectsTest {

    @Test
    fun extractR2dbcDriver() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_ssl() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbcs:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_testcontainers() {
        val driver = R2dbcDialects.extractR2dbcDriver("r2dbc:tc:h2:mem:///testdb")
        assertEquals("h2", driver)
    }

    @Test
    fun extractR2dbcDriver_error() {
        assertFailsWith<IllegalStateException> {
            R2dbcDialects.extractR2dbcDriver("jdbc:h2:mem:///testdb")
        }
    }
}
