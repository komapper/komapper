package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import org.komapper.tx.core.TransactionOperator
import java.sql.Connection
import javax.sql.DataSource

/**
 * Represents a session for JDBC access.
 */
@ThreadSafe
interface JdbcSession {
    val dataSource: DataSource

    val transactionOperator: TransactionOperator

    /**
     * Returns a JDBC connection.
     */
    fun getConnection(): Connection

    /**
     * Releases a JDBC connection.
     */
    fun releaseConnection(connection: Connection)

    /**
     * Uses a JDBC connection.
     */
    fun <R> useConnection(block: (Connection) -> R): R {
        val con = getConnection()
        return runCatching {
            block(con)
        }.onSuccess {
            releaseConnection(con)
        }.onFailure { cause ->
            runCatching {
                releaseConnection(con)
            }.onFailure {
                cause.addSuppressed(it)
            }
        }.getOrThrow()
    }
}

class DefaultJdbcSession(override val dataSource: DataSource) : JdbcSession {
    override val transactionOperator: TransactionOperator
        get() = throw UnsupportedOperationException("Use a module that provides transaction management.")

    override fun getConnection(): Connection {
        return dataSource.connection
    }

    override fun releaseConnection(connection: Connection) {
        connection.close()
    }
}
