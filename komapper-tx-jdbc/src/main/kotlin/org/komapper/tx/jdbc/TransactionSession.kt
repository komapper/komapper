package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.jdbc.JdbcSession
import java.sql.Connection
import javax.sql.DataSource

/**
 * Represents a transactional session for JDBC.
 */
class TransactionSession(
    private val dataSource: DataSource,
    private val loggerFacade: LoggerFacade,
    private val isolationLevel: IsolationLevel? = null
) : JdbcSession {
    override val connection: Connection
        get() =
            transactionManager.dataSource.connection
    val userTransaction: UserTransaction by lazy {
        TransactionScopeImpl(transactionManager, isolationLevel)
    }
    val transactionManager: TransactionManager by lazy {
        TransactionManagerImpl(dataSource, loggerFacade)
    }
}
