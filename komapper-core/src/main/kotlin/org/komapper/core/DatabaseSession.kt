package org.komapper.core

import org.komapper.core.tx.TransactionIsolationLevel
import org.komapper.core.tx.TransactionManager
import org.komapper.core.tx.TransactionManagerImpl
import org.komapper.core.tx.TransactionScopeImpl
import org.komapper.core.tx.UserTransaction
import java.sql.Connection
import javax.sql.DataSource

interface DatabaseSession {
    val connection: Connection
    val userTransaction: UserTransaction?
    val transactionManager: TransactionManager?
}

class DefaultDatabaseSession(private val dataSource: DataSource) : DatabaseSession {
    override val connection: Connection get() = dataSource.connection
    override val userTransaction: UserTransaction? = null
    override val transactionManager: TransactionManager? = null
}

class TransactionalDatabaseSession(
    private val dataSource: DataSource,
    private val logger: Logger,
    private val isolationLevel: TransactionIsolationLevel? = null
) : DatabaseSession {
    override val connection: Connection
        get() =
            transactionManager.dataSource.connection
    override val userTransaction: UserTransaction by lazy {
        TransactionScopeImpl(transactionManager, isolationLevel)
    }
    override val transactionManager: TransactionManager by lazy {
        TransactionManagerImpl(dataSource, logger)
    }
}
