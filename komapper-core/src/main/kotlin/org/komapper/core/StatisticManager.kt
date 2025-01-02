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
 * Creates an instance of `StatisticManager`.
 *
 * This method constructs a default implementation of the `StatisticManager`
 * interface, allowing users to manage SQL execution statistics, such as enabling
 * or disabling statistics collection, retrieving statistics, and clearing data.
 *
 * @param enabled A boolean that determines whether the statistics collection is
 * initially enabled. Defaults to `false`.
 * @return An instance of the `StatisticManager` interface.
 */
fun StatisticManager(enabled: Boolean = false): StatisticManager {
    return DefaultStatisticManager(enabled)
}

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
