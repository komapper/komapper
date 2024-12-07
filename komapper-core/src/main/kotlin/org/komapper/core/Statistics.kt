package org.komapper.core

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Statistics.
 */
interface Statistics {
    /**
     * Returns true if statistics is enabled.
     * @return true if statistics is enabled
     */
    fun isEnabled(): Boolean

    /**
     * Sets the statistics enabled.
     * @param enabled true if statistics is enabled
     */
    fun setEnabled(enabled: Boolean)

    /**
     * Returns the SQL statistics.
     * @param sql The SQL
     * @return The SQL statistics
     */
    fun getSqlStatistics(sql: String): SqlStatistics?

    /**
     * Returns all SQL statistics.
     * @return All SQL statistics
     */
    fun getAllSqlStatistics(): Map<String, SqlStatistics>

    /**
     * Adds the execution time.
     * @param sql The SQL
     * @param startTime The start time in nanoseconds
     * @param endTime The end time in nanoseconds
     * @return The result
     */
    fun add(sql: String, startTime: Long, endTime: Long)

    /**
     * Clears all statistics.
     */
    fun clear()
}

class DefaultStatistics : Statistics {
    @Volatile
    private var enabled = false
    private val sqlStatisticsMap = ConcurrentHashMap<String, MutableSqlStatistics>()

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun getSqlStatistics(sql: String): SqlStatistics? {
        return sqlStatisticsMap[sql]
    }

    override fun getAllSqlStatistics(): Map<String, SqlStatistics> {
        return sqlStatisticsMap
    }

    override fun add(sql: String, startTime: Long, endTime: Long) {
        if (!enabled) return
        val milliseconds = TimeUnit.NANOSECONDS.toMillis(endTime - startTime)
        val sqlStatistics = sqlStatisticsMap.getOrPut(sql, ::DefaultMutableSqlStatistics)
        sqlStatistics.add(milliseconds)
    }

    override fun clear() {
        sqlStatisticsMap.clear()
    }

    override fun toString(): String {
        return "Statistics(enabled=$enabled, sqlStatisticsMap.size=${sqlStatisticsMap.size})"
    }
}

internal object EmptyStatistics : Statistics {
    override fun isEnabled(): Boolean = false

    override fun setEnabled(enabled: Boolean) {
    }

    override fun getSqlStatistics(sql: String): SqlStatistics {
        return EmptySqlStatistics
    }

    override fun getAllSqlStatistics(): Map<String, SqlStatistics> {
        return emptyMap()
    }

    override fun add(sql: String, startTime: Long, endTime: Long) {
    }

    override fun clear() {
    }
}
