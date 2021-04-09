package org.komapper.core

import org.komapper.core.tx.DefaultTransactionManager
import org.komapper.core.tx.DefaultUserTransaction
import org.komapper.core.tx.EmptyTransactionManager
import org.komapper.core.tx.EmptyUserTransaction
import org.komapper.core.tx.TransactionIsolationLevel
import org.komapper.core.tx.TransactionManager
import org.komapper.core.tx.UserTransaction
import java.sql.Connection
import javax.sql.DataSource

interface DatabaseSession {
    fun getConnection(): Connection
    fun getUserTransaction(): UserTransaction
    fun getTransactionManager(): TransactionManager
}

class DefaultDatabaseSession(private val dataSource: DataSource) : DatabaseSession {
    override fun getConnection(): Connection {
        return dataSource.connection
    }

    override fun getUserTransaction(): UserTransaction {
        return EmptyUserTransaction()
    }

    override fun getTransactionManager(): TransactionManager {
        return EmptyTransactionManager(dataSource)
    }
}

class TransactionalDatabaseSession(
    private val dataSource: DataSource,
    private val logger: Logger,
    private val isolationLevel: TransactionIsolationLevel? = null
) : DatabaseSession {

    private val txManager: TransactionManager by lazy {
        DefaultTransactionManager(dataSource, logger)
    }

    override fun getConnection(): Connection {
        return txManager.getDataSource().connection
    }

    override fun getUserTransaction(): UserTransaction {
        return DefaultUserTransaction(txManager, isolationLevel)
    }

    override fun getTransactionManager(): TransactionManager {
        return txManager
    }
}
