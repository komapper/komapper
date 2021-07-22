package org.komapper.core

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

internal class ExecutionOptionsTest {

    @Test
    fun plus() {
        val a = ExecutionOptions(batchSize = 1, fetchSize = 1)
        val b = ExecutionOptions(fetchSize = 2, maxRows = 2)
        val c = a + b
        assertEquals(1, c.batchSize)
        assertEquals(2, c.fetchSize)
        assertEquals(2, c.maxRows)
    }
}