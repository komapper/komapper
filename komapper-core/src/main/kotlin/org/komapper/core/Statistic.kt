package org.komapper.core

/**
 * Represents a set of statistics for SQL execution.
 *
 * The `Statistic` interface provides methods and properties to collect
 * and compute execution statistics, such as the count of executions,
 * maximum execution time, minimum execution time, total execution time,
 * and average execution time. It also supports the calculation of new
 * statistics based on additional execution data.
 */
interface Statistic {
    val sql: String
    val execCount: Long
    val execMaxTime: Long
    val execMinTime: Long
    val execTotalTime: Long
    val execAvgTime: Double

    fun calculate(execTimeMillis: Long): Statistic
}

private data class StatisticImpl(
    override val sql: String,
    override val execCount: Long = 0,
    override val execMaxTime: Long = 0,
    override val execMinTime: Long = Long.MAX_VALUE,
    override val execTotalTime: Long = 0,
    override val execAvgTime: Double = 0.0,
) : Statistic {
    override fun calculate(execTimeMillis: Long): Statistic {
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
}

/**
 * Creates a new instance of the `Statistic` interface with the given SQL string and execution time.
 *
 * @param sql the SQL statement string associated with this statistic
 * @param execTimeMillis the execution time in milliseconds for the given SQL statement
 * @return a new `Statistic` instance representing the initial execution statistics
 */
fun Statistic(sql: String, execTimeMillis: Long): Statistic {
    return StatisticImpl(
        sql = sql,
        execCount = 1,
        execMaxTime = execTimeMillis,
        execMinTime = execTimeMillis,
        execTotalTime = execTimeMillis,
        execAvgTime = execTimeMillis.toDouble()
    )
}
