package org.komapper.core

import kotlin.test.Test
import kotlin.test.assertEquals

class NamingStrategyTest {
    @Test
    fun testCamelToLowerSnakeCase() {
        val strategy = CamelToLowerSnakeCase
        assertEquals("", strategy.apply(""))
        assertEquals("aaa_bbb_ccc", strategy.apply("aaaBbbCcc"))
        assertEquals("abc", strategy.apply("abc"))
        assertEquals("aa1_bbb_ccc", strategy.apply("aa1BbbCcc"))
        assertEquals("sql", strategy.apply("SQL"))
    }

    @Test
    fun testCamelToUpperSnakeCase() {
        val strategy = CamelToUpperSnakeCase
        assertEquals("", strategy.apply(""))
        assertEquals("AAA_BBB_CCC", strategy.apply("aaaBbbCcc"))
        assertEquals("ABC", strategy.apply("abc"))
        assertEquals("AA1_BBB_CCC", strategy.apply("aa1BbbCcc"))
        assertEquals("SQL", strategy.apply("SQL"))
    }

    @Test
    fun testSnakeToLowerCamelCase() {
        val strategy = SnakeToLowerCamelCase
        assertEquals("", strategy.apply(""))
        assertEquals("aaaBbbCcc", strategy.apply("aaa_bbb_ccc"))
        assertEquals("abc", strategy.apply("abc"))
        assertEquals("aa1BbbCcc", strategy.apply("aa1_bbb_ccc"))
    }

    @Test
    fun testSnakeToUpperCamelCase() {
        val strategy = SnakeToUpperCamelCase
        assertEquals("", strategy.apply(""))
        assertEquals("AaaBbbCcc", strategy.apply("aaa_bbb_ccc"))
        assertEquals("Abc", strategy.apply("abc"))
        assertEquals("Aa1BbbCcc", strategy.apply("aa1_bbb_ccc"))
    }
}
