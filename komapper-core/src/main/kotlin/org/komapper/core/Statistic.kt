package org.komapper.core

/**
 * Represents statistics associated with the execution of an SQL statement.
 *
 * @property sql the SQL statement being tracked
 * @property execCount the number of times the SQL statement has been executed
 * @property execMaxTime the maximum execution time of the SQL statement in milliseconds
 * @property execMinTime the minimum execution time of the SQL statement in milliseconds
 * @property execTotalTime the total execution time of the SQL statement in milliseconds
 * @property execAvgTime the average execution time of the SQL statement in milliseconds
 */
data class Statistic(
    val sql: String,
    val execCount: Long,
    val execMaxTime: Long,
    val execMinTime: Long,
    val execTotalTime: Long,
    val execAvgTime: Double,
) {
    /**
     * Updates the execution statistics with a new execution time and returns the updated statistics.
     *
     * @param execTimeMillis the execution time in milliseconds of the latest execution
     * @return the updated statistic with the new execution time included
     */
    fun calculate(execTimeMillis: Long): Statistic {
        val count = execCount + 1
        val totalTime = execTotalTime + execTimeMillis
        val avgTime = totalTime / count.toDouble()
        return copy(
            execCount = count,
            execMaxTime = maxOf(execMaxTime, execTimeMillis),
            execMinTime = minOf(execMinTime, execTimeMillis),
            execTotalTime = totalTime,
            execAvgTime = avgTime
        )
    }

    companion object {
        /**
         * Creates a new instance of the Statistic class with initial execution statistics.
         *
         * @param sql the SQL statement being tracked
         * @param execTimeMillis the execution time of the SQL statement in milliseconds
         * @return a new instance of the Statistic class initialized with the given parameters
         */
        fun of(sql: String, execTimeMillis: Long): Statistic {
            return Statistic(
                sql = sql,
                execCount = 1,
                execMaxTime = execTimeMillis,
                execMinTime = execTimeMillis,
                execTotalTime = execTimeMillis,
                execAvgTime = execTimeMillis.toDouble()
            )
        }
    }
}
