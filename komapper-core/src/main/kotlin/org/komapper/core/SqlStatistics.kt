package org.komapper.core

/**
 * SQL statistics.
 *
 * @property executionCount The number of SQL executions.
 * @property executionMaxTime The maximum SQL execution time in milliseconds.
 * @property executionMinTime The minimum SQL execution time in milliseconds.
 * @property executionTotalTime The total SQL execution time in milliseconds.
 * @property executionAvgTime The average SQL execution time in milliseconds.
 */
data class SqlStatistics(
    val executionCount: Long = 0,
    val executionMaxTime: Long = 0,
    val executionMinTime: Long = Long.MAX_VALUE,
    val executionTotalTime: Long = 0,
    val executionAvgTime: Double = 0.0,
)
