package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import java.sql.Connection
import javax.sql.DataSource

/**
 * Represents a session for JDBC access.
 */
@ThreadSafe
interface JdbcSession {

    val transactionOperator: JdbcTransactionOperator

    /**
     * Returns a JDBC connection.
     */
    fun getConnection(): Connection
}

class DefaultJdbcSession(private val dataSource: DataSource) : JdbcSession {
    override val transactionOperator: JdbcTransactionOperator
        get() = throw UnsupportedOperationException("Use a module that provides transaction management.")

    override fun getConnection(): Connection {
        return dataSource.connection
    }
}
