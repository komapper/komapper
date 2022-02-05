package org.komapper.processor

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProcessorUtilityTest {

    @Test
    fun toPropertyNameFormat() {
        assertEquals("aaaBbbCcc", toCamelCase("AaaBbbCcc"))
        assertEquals("aa5BbbCcc", toCamelCase("Aa5BbbCcc"))
        assertEquals("a5aBbbCcc", toCamelCase("A5aBbbCcc"))
        assertEquals("vAddress", toCamelCase("VAddress"))
        assertEquals("uuidTest", toCamelCase("UUIDTest"))
    }
}
