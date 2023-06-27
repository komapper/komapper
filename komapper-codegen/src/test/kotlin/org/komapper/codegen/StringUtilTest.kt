package org.komapper.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilTest {

    @Test
    fun snakeToUpperCamelCase() {
        assertEquals("", StringUtil.snakeToUpperCamelCase(""))
        assertEquals("AaaBbbCcc", StringUtil.snakeToUpperCamelCase("aaa_bbb_ccc"))
        assertEquals("Abc", StringUtil.snakeToUpperCamelCase("abc"))
        assertEquals("Aa1BbbCcc", StringUtil.snakeToUpperCamelCase("aa1_bbb_ccc"))
    }

    @Test
    fun snakeToLowerCamelCase() {
        assertEquals("", StringUtil.snakeToLowerCamelCase(""))
        assertEquals("aaaBbbCcc", StringUtil.snakeToLowerCamelCase("aaa_bbb_ccc"))
        assertEquals("abc", StringUtil.snakeToLowerCamelCase("abc"))
        assertEquals("aa1BbbCcc", StringUtil.snakeToLowerCamelCase("aa1_bbb_ccc"))
    }

}
