package org.komapper.core.dsl.operator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiteralExpressionsTest {
    @Test
    fun literal_int() {
        assertEquals(literal(123), literal(123))
    }

    @Test
    fun literal_string() {
        assertEquals(literal("abc"), literal("abc"))
    }

    @Test
    fun literal_string_null() {
        val value: String? = null
        assertEquals(literal(value), literal(value))
    }

    @Test
    fun literal_string_illegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            literal("This isn't legal")
        }
    }

    @Test
    fun testContainsSingleQuote() {
        assertTrue(containsSingleQuote("'unclosed"))
        assertTrue(containsSingleQuote("unclosed'"))
        assertTrue(containsSingleQuote("'single quoted'"))
        assertTrue(containsSingleQuote("text 'with' quotes"))
        assertTrue(containsSingleQuote("'"))
        assertTrue(containsSingleQuote("'''"))

        assertFalse(containsSingleQuote("no quotes here"))
        assertFalse(containsSingleQuote(""))
        assertFalse(containsSingleQuote("''"))
        assertFalse(containsSingleQuote("''''"))
    }
}
