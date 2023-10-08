package org.komapper.core.dsl.operator

import kotlin.test.Test
import kotlin.test.assertEquals

class LiteralExpressionsTest {

    @Test
    fun literal_int() {
        assertEquals(literal(123), literal(123))
    }
}
