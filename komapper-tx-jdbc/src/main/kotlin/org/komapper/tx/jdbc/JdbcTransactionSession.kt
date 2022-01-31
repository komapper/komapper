package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.jdbc.JdbcSession
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
    override val connection: Connection
        get() =
            transactionManager.dataSource.connection
    val userTransaction: JdbcUserTransaction by lazy {
        JdbcTransactionScopeImpl(transactionManager, isolationLevel)
    }
    val transactionManager: JdbcTransactionManager by lazy {
        JdbcTransactionManagerImpl(dataSource, loggerFacade)
    }
}
