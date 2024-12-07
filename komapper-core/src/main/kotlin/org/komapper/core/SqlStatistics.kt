package org.komapper.core

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * SQL statistics.
 */
interface SqlStatistics {
    /**
     * The number of SQL executions.
     */
    val executionCount: Long

    /**
     * The maximum SQL execution time in milliseconds.
     */
    val executionMaxTime: Long

    /**
     * The minimum SQL execution time in milliseconds.
     */
    val executionMinTime: Long

    /**
     * The total SQL execution time in milliseconds.
     */
    val executionTotalTime: Long

    /**
     * The average SQL execution time in milliseconds.
     */
    val executionAvgTime: Double
}

interface MutableSqlStatistics : SqlStatistics {
    /**
     * Adds the execution time.
     * @param time The execution time in milliseconds.
     */
    fun add(time: Long)
}

internal class DefaultMutableSqlStatistics : MutableSqlStatistics {
    private val lock = ReentrantReadWriteLock()
    private val readLock = lock.readLock()
    private val writeLock = lock.writeLock()

    private val count = LongAdder()
    private val maxTime = AtomicLong()
    private val minTime = AtomicLong(Long.MAX_VALUE)
    private val totalTime = LongAdder()

    override val executionCount: Long
        get() = count.toLong()

    override val executionMaxTime: Long
        get() = maxTime.toLong()

    override val executionMinTime: Long
        get() = minTime.toLong()

    override val executionTotalTime: Long
        get() = totalTime.toLong()

    override val executionAvgTime: Double
        get() {
            writeLock.withLock {
                val count = count.toLong()
                return if (count == 0L) {
                    0.0
                } else {
                    totalTime.toLong() / count.toDouble()
                }
            }
        }

    override fun add(time: Long) {
        maxTime.accumulateAndGet(time, ::maxOf)
        minTime.accumulateAndGet(time, ::minOf)
        readLock.withLock {
            count.increment()
            totalTime.add(time)
        }
    }

    override fun toString(): String {
        return "SqlStatistics(" +
            "executionCount=$executionCount, " +
            "executionMaxTime=$executionMaxTime, " +
            "executionMinTime=$executionMinTime, " +
            "executionTotalTime=$executionTotalTime, " +
            "executionAvgTime=$executionAvgTime" +
            ")"
    }
}

internal object EmptySqlStatistics : SqlStatistics {
    override val executionCount: Long = 0
    override val executionMaxTime: Long = 0
    override val executionMinTime: Long = 0
    override val executionTotalTime: Long = 0
    override val executionAvgTime: Double = 0.0
}
