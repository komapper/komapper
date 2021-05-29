package org.komapper.tx.jdbc

import org.komapper.core.Logger
import org.komapper.jdbc.DatabaseSession
import java.sql.Connection
import javax.sql.DataSource

class TransactionDatabaseSession(
    private val dataSource: DataSource,
    private val logger: Logger,
    private val isolationLevel: TransactionIsolationLevel? = null
) : DatabaseSession {
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
