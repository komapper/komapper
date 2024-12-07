package org.komapper.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultStatisticsTest {
    @Test
    fun add_disabled() {
        val stats = DefaultStatistics()
        stats.add("a", 100, 1_000)
        stats.add("b", 200, 900)
        stats.add("c", 300, 800)
        assertFalse(stats.isEnabled())
        assertEquals(0, stats.getAllSqlStatistics().size)
    }

    @Test
    fun add_enabled() {
        val stats = DefaultStatistics()
        stats.setEnabled(true)
        stats.add("a", 1_000_000, 10_000_000)
        stats.add("b", 2_000_000, 9_000_000)
        stats.add("c", 3_000_000, 8_000_000)
        stats.add("a", 4_000_000, 7_000_000)
        stats.add("b", 5_000_000, 6_000_000)
        assertTrue(stats.isEnabled())
        assertEquals(3, stats.getAllSqlStatistics().size)

        val a = stats.getSqlStatistics("a")
        assertNotNull(a)
        assertEquals(2, a.executionCount)
        assertEquals(9, a.executionMaxTime)
        assertEquals(3, a.executionMinTime)
        assertEquals(12, a.executionTotalTime)

        val b = stats.getSqlStatistics("b")
        assertNotNull(b)
        assertEquals(2, b.executionCount)
        assertEquals(7, b.executionMaxTime)
        assertEquals(1, b.executionMinTime)
        assertEquals(8, b.executionTotalTime)

        val c = stats.getSqlStatistics("c")
        assertNotNull(c)
        assertEquals(1, c.executionCount)
        assertEquals(5, c.executionMaxTime)
        assertEquals(5, c.executionMinTime)
        assertEquals(5, c.executionTotalTime)
    }

    @Test
    fun clear() {
        val stats = DefaultStatistics()
        stats.setEnabled(true)
        stats.add("a", 1_000_000, 10_000_000)
        stats.add("b", 2_000_000, 9_000_000)
        assertEquals(2, stats.getAllSqlStatistics().size)
        stats.clear()
        assertEquals(0, stats.getAllSqlStatistics().size)
    }

    @Test
    fun getSqlStatistics() {
        val stats = DefaultStatistics()
        stats.setEnabled(true)
        stats.add("a", 1_000_000, 10_000_000)

        val a = stats.getSqlStatistics("a")
        assertNotNull(a)
        val b = stats.getSqlStatistics("b")
        assertNull(b)
    }
}
