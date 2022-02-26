package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.jdbc.JdbcIsolationLevel
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.JdbcTransactionalOperator
import java.sql.Connection
import javax.sql.DataSource

/**
 * Represents a transactional session for JDBC.
 */
class JdbcTransactionSession(
    private val dataSource: DataSource,
    private val loggerFacade: LoggerFacade,
    private val isolationLevel: JdbcIsolationLevel? = null
) : JdbcSession {

    val transactionManager: JdbcTransactionManager by lazy {
        JdbcTransactionManagerImpl(dataSource, loggerFacade)
    }

    override val transactionalOperator: JdbcTransactionalOperator by lazy {
        JdbcTransactionalOperatorImpl(transactionManager, isolationLevel)
    }

    override fun getConnection(): Connection {
        return transactionManager.dataSource.connection
    }
}
