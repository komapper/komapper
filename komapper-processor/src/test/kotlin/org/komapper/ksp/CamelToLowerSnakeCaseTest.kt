package org.komapper.ksp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CamelToLowerSnakeCaseTest {

    private val strategy = CamelToLowerSnakeCase

    @Test
    fun apply() {
        assertEquals("", strategy.apply(""))
        assertEquals("aaa_bbb_ccc", strategy.apply("aaaBbbCcc"))
        assertEquals("abc", strategy.apply("abc"))
        assertEquals("aa1_bbb_ccc", strategy.apply("aa1BbbCcc"))
        assertEquals("sql", strategy.apply("SQL"))
    }
}
