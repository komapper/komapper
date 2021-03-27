package org.komapper.ksp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CamelToUpperSnakeCaseTest {

    private val strategy = CamelToUpperSnakeCase

    @Test
    fun apply() {
        assertEquals("", strategy.apply(""))
        assertEquals("AAA_BBB_CCC", strategy.apply("aaaBbbCcc"))
        assertEquals("ABC", strategy.apply("abc"))
        assertEquals("AA1_BBB_CCC", strategy.apply("aa1BbbCcc"))
        assertEquals("SQL", strategy.apply("SQL"))
    }
}
