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

    val transactionOperator: TransactionOperator

    /**
     * Returns a JDBC connection.
     */
    fun getConnection(): Connection

    /**
     * Releases a JDBC connection.
     */
    fun releaseConnection(connection: Connection)
}

class DefaultJdbcSession(private val dataSource: DataSource) : JdbcSession {
    override val transactionOperator: TransactionOperator
        get() = throw UnsupportedOperationException("Use a module that provides transaction management.")

    override fun getConnection(): Connection {
        return dataSource.connection
    }

    override fun releaseConnection(connection: Connection) {
        connection.close()
    }
}
