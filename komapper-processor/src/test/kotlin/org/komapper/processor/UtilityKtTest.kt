package org.komapper.processor

import kotlin.test.Test
import kotlin.test.assertEquals

internal class UtilityKtTest {

    @Test
    fun toPropertyNameFormat() {
        assertEquals("aaaBbbCcc", toPropertyNameFormat("AaaBbbCcc"))
        assertEquals("aa5BbbCcc", toPropertyNameFormat("Aa5BbbCcc"))
        assertEquals("a5aBbbCcc", toPropertyNameFormat("A5aBbbCcc"))
        assertEquals("vAddress", toPropertyNameFormat("VAddress"))
        assertEquals("uuidTest", toPropertyNameFormat("UUIDTest"))
    }
}
