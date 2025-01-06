package org.komapper.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultStatisticManagerTest {
    @Test
    fun recordSqlExecution_invalidTime_shouldThrowException() {
        val stats = DefaultStatisticManager()
        stats.setEnabled(true)
        assertFailsWith<IllegalArgumentException> {
            stats.recordSqlExecution("invalid", 10_000_000, 5_000_000) // endTimeNanos < startTimeNanos
        }
    }

    @Test
    fun getStatistics_noRecordings_shouldReturnEmptyIterable() {
        val stats = DefaultStatisticManager()
        stats.setEnabled(true)
        assertTrue(stats.getStatistics().none()) // Should be empty
    }

    @Test
    fun enableDisableStatisticManager_shouldReflectState() {
        val stats = DefaultStatisticManager()
        assertFalse(stats.isEnabled())

        stats.setEnabled(true)
        assertTrue(stats.isEnabled())

        stats.setEnabled(false)
        assertFalse(stats.isEnabled())
    }

    @Test
    fun recordSqlExecution_disabled() {
        val stats = DefaultStatisticManager()
        stats.recordSqlExecution("a", 100, 1_000)
        stats.recordSqlExecution("b", 200, 900)
        stats.recordSqlExecution("c", 300, 800)
        assertFalse(stats.isEnabled())
        assertEquals(0, stats.getStatistics().count())
    }

    @Test
    fun recordSqlExecution_enabled() {
        val stats = DefaultStatisticManager()
        stats.setEnabled(true)
        stats.recordSqlExecution("a", 1_000_000, 10_000_000)
        stats.recordSqlExecution("b", 2_000_000, 9_000_000)
        stats.recordSqlExecution("c", 3_000_000, 8_000_000)
        stats.recordSqlExecution("a", 4_000_000, 7_000_000)
        stats.recordSqlExecution("b", 5_000_000, 6_000_000)
        assertTrue(stats.isEnabled())
        assertEquals(3, stats.getStatistics().count())

        val a = stats.getStatistics().single { it.sql == "a" }
        assertNotNull(a)
        assertEquals(2, a.execCount)
        assertEquals(9, a.execMaxTime)
        assertEquals(3, a.execMinTime)
        assertEquals(12, a.execTotalTime)

        val b = stats.getStatistics().single { it.sql == "b" }
        assertNotNull(b)
        assertEquals(2, b.execCount)
        assertEquals(7, b.execMaxTime)
        assertEquals(1, b.execMinTime)
        assertEquals(8, b.execTotalTime)

        val c = stats.getStatistics().single { it.sql == "c" }
        assertNotNull(c)
        assertEquals(1, c.execCount)
        assertEquals(5, c.execMaxTime)
        assertEquals(5, c.execMinTime)
        assertEquals(5, c.execTotalTime)
    }

    @Test
    fun clear() {
        val stats = DefaultStatisticManager()
        stats.setEnabled(true)
        stats.recordSqlExecution("a", 1_000_000, 10_000_000)
        stats.recordSqlExecution("b", 2_000_000, 9_000_000)
        assertEquals(2, stats.getStatistics().count())
        stats.clear()
        assertEquals(0, stats.getStatistics().count())
    }

    @Test
    fun getSqlStatistics() {
        val stats = DefaultStatisticManager()
        stats.setEnabled(true)
        stats.recordSqlExecution("a", 1_000_000, 10_000_000)

        val a = stats.getStatistics().single { it.sql == "a" }
        assertNotNull(a)
        val b = stats.getStatistics().singleOrNull { it.sql == "b" }
        assertNull(b)
    }

    @Test
    fun recordSqlExecution() = runBlocking {
        val stats = DefaultStatisticManager()
        stats.setEnabled(true)

        val jobs = mutableListOf<Job>()
        for (i in 1..4) {
            val job = launch(Dispatchers.Default) {
                stats.recordSqlExecution("sql", 0, i * 1_000_000_000L)
            }
            jobs.add(job)
        }
        jobs.forEach { it.join() }

        val sqlStats = stats.getStatistics().first()
        assertNotNull(sqlStats)
        assertEquals(4, sqlStats.execCount)
        assertEquals(4_000, sqlStats.execMaxTime)
        assertEquals(1_000, sqlStats.execMinTime)
        assertEquals(10_000, sqlStats.execTotalTime)
        assertEquals(2_500.0, sqlStats.execAvgTime)
    }
}
