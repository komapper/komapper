package org.komapper.core

import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun getValidBatchSize_default() {
        val options = ExecutionOptions()
        assertEquals(10, options.getValidBatchSize())
    }

    @Test
    fun getValidBatchSize_zero() {
        val options = ExecutionOptions(batchSize = 0)
        assertEquals(10, options.getValidBatchSize())
    }

    @Test
    fun getValidBatchSize_negativeNumber() {
        val options = ExecutionOptions(batchSize = -1)
        assertEquals(10, options.getValidBatchSize())
    }

    @Test
    fun getValidBatchSize_positiveNumber() {
        val options = ExecutionOptions(batchSize = 1)
        assertEquals(1, options.getValidBatchSize())
    }
}
