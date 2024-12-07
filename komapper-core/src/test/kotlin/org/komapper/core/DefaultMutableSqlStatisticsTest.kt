package org.komapper.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultMutableSqlStatisticsTest {
    @Test
    fun add() = runBlocking {
        val stats = DefaultMutableSqlStatistics()

        val jobs = mutableListOf<Job>()
        for (i in 1..4) {
            val job = launch(Dispatchers.Default) {
                stats.add(i * 1000L)
            }
            jobs.add(job)
        }
        jobs.forEach { it.join() }

        assertEquals(4, stats.executionCount)
        assertEquals(4_000, stats.executionMaxTime)
        assertEquals(1_000, stats.executionMinTime)
        assertEquals(10_000, stats.executionTotalTime)
        assertEquals(2_500.0, stats.executionAvgTime)
    }
}
