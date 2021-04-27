package org.komapper.core

/**
 * @property batchSize the batch size. This value is used for batch commands.
 * @property fetchSize the fetch size. See [java.sql.PreparedStatement.setFetchSize].
 * @property maxRows the max rows. See [java.sql.PreparedStatement.setMaxRows].
 * @property queryTimeoutSeconds the query timeout. See [java.sql.PreparedStatement.setQueryTimeout].
 * @property suppressLogging whether to suppress SQL logging.
 */
data class JdbcOption(
    val batchSize: Int? = null,
    val fetchSize: Int? = null,
    val maxRows: Int? = null,
    val queryTimeoutSeconds: Int? = null,
    val suppressLogging: Boolean? = null
) {
    infix operator fun plus(other: JdbcOption): JdbcOption {
        return JdbcOption(
            other.batchSize ?: this.batchSize,
            other.fetchSize ?: this.fetchSize,
            other.maxRows ?: this.maxRows,
            other.queryTimeoutSeconds ?: this.queryTimeoutSeconds,
            other.suppressLogging ?: this.suppressLogging
        )
    }
}

interface JdbcOptionProvider {
    fun getJdbcOption(): JdbcOption
}
