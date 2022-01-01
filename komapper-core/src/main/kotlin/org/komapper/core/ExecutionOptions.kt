package org.komapper.core

/**
 * The options for sql execution.
 * @property batchSize the batch size. This value is used for batch commands.
 * @property fetchSize the fetch size.
 * @property maxRows the max rows.
 * @property queryTimeoutSeconds the query timeout.
 * @property suppressLogging whether to suppress SQL logging.
 */
@ThreadSafe
data class ExecutionOptions(
    val batchSize: Int? = null,
    val fetchSize: Int? = null,
    val maxRows: Int? = null,
    val queryTimeoutSeconds: Int? = null,
    val suppressLogging: Boolean? = null
) {
    infix operator fun plus(other: ExecutionOptions): ExecutionOptions {
        return ExecutionOptions(
            other.batchSize ?: this.batchSize,
            other.fetchSize ?: this.fetchSize,
            other.maxRows ?: this.maxRows,
            other.queryTimeoutSeconds ?: this.queryTimeoutSeconds,
            other.suppressLogging ?: this.suppressLogging
        )
    }
}

/**
 * The provider of [ExecutionOptions].
 */
@ThreadSafe
interface ExecutionOptionsProvider {
    fun getExecutionOptions(): ExecutionOptions
}
