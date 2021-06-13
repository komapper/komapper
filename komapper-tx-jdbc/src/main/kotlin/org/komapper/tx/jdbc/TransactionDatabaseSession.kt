package org.komapper.tx.jdbc

import org.komapper.core.Logger
import org.komapper.jdbc.JdbcDatabaseSession
import java.sql.Connection
import javax.sql.DataSource

class TransactionDatabaseSession(
    private val dataSource: DataSource,
    private val logger: Logger,
    private val isolationLevel: IsolationLevel? = null
) : JdbcDatabaseSession {
    override val connection: Connection
        get() =
            transactionManager.dataSource.connection
    val userTransaction: UserTransaction by lazy {
        TransactionScopeImpl(transactionManager, isolationLevel)
    }
    val transactionManager: TransactionManager by lazy {
        TransactionManagerImpl(dataSource, logger)
    }
}
