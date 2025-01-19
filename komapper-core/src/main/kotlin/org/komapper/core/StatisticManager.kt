package org.komapper.core

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Manages the collection and reporting of SQL execution statistics.
 *
 * This interface allows enabling or disabling the statistics collection,
 * retrieving the collected statistics, recording SQL execution details, and
 * clearing the statistics data.
 */
@ThreadSafe
interface StatisticManager {
    /**
     * Checks whether the statistics collection is enabled.
     *
     * @return true if the statistics collection is enabled, false otherwise
     */
    fun isEnabled(): Boolean

    /**
     * Enables or disables the statistics collection.
     *
     * @param enabled true to enable statistics collection, false to disable it
     */
    fun setEnabled(enabled: Boolean)

    /**
     * Retrieves the collection of SQL execution statistics.
     *
     * @return an iterable collection of statistics, where each statistic contains
     * details such as the executed SQL statement, execution count, maximum execution time,
     * minimum execution time, total execution time, and average execution time
     */
    fun getStatistics(): Iterable<Statistic>

    /**
     * Records the execution of an SQL query and updates the associated statistics.
     *
     * @param sql the SQL statement that was executed
     * @param startTimeNanos the start time of the execution in nanoseconds
     * @param endTimeNanos the end time of the execution in nanoseconds
     */
    fun recordSqlExecution(sql: String, startTimeNanos: Long, endTimeNanos: Long)

    /**
     * Clears all collected SQL execution statistics.
     *
     * This method removes all the data gathered during SQL execution monitoring,
     * resetting the statistics to an initial empty state.
     */
    fun clear()
}

/**
 * Provides a thread-safe implementation for managing the collection and reporting
 * of SQL execution statistics. This class is responsible for tracking execution
 * details such as execution time and occurrences, and maintains a record of statistics
 * for each unique SQL statement.
 *
 * @property enabled Determines whether statistics collection is currently enabled. It is volatile to allow
 * thread-safe toggling of this feature.
 * @constructor Initializes the statistics manager with the given state for enabling or disabling statistics collection.
 *
 * This class uses a concurrent map to ensure thread-safe access and updates to stored statistics.
 * It verifies input constraints, such as ensuring valid execution time ranges, and updates or creates statistics
 * accordingly when a new SQL execution is recorded.
 */
internal class DefaultStatisticManager(
    @Volatile private var enabled: Boolean = false,
) : StatisticManager {
    private val statisticMap = ConcurrentHashMap<String, Statistic>()

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun getStatistics(): Iterable<Statistic> {
        return statisticMap.values
    }

    override fun recordSqlExecution(sql: String, startTimeNanos: Long, endTimeNanos: Long) {
        if (!enabled) return
        if (endTimeNanos < startTimeNanos) throw IllegalArgumentException("endTimeNanos < startTimeNanos")
        val execTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTimeNanos - startTimeNanos)
        statisticMap.compute(sql) { k, v ->
            v?.calculate(execTimeMillis) ?: Statistic.of(k, execTimeMillis)
        }
    }

    override fun clear() {
        statisticMap.clear()
    }

    override fun toString(): String {
        return "StatisticManager(enabled=$enabled)"
    }
}

internal object EmptyStatisticManager : StatisticManager {
    override fun isEnabled(): Boolean = false

    override fun setEnabled(enabled: Boolean) {
    }

    override fun getStatistics(): Iterable<Statistic> {
        return emptyList()
    }

    override fun recordSqlExecution(sql: String, startTimeNanos: Long, endTimeNanos: Long) {
    }

    override fun clear() {
    }
}
