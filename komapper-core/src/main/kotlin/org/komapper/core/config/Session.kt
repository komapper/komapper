package org.komapper.core.config

import org.komapper.core.tx.DefaultTransactionManager
import org.komapper.core.tx.DefaultUserTransaction
import org.komapper.core.tx.EmptyTransactionManager
import org.komapper.core.tx.EmptyUserTransaction
import org.komapper.core.tx.TransactionIsolationLevel
import org.komapper.core.tx.TransactionManager
import org.komapper.core.tx.UserTransaction
import java.sql.Connection
import javax.sql.DataSource

interface Session {
    fun getConnection(): Connection
    fun getUserTransaction(): UserTransaction
    fun getTransactionManager(): TransactionManager
}

class DefaultSession(private val dataSource: DataSource) : Session {
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

class TransactionalSession(
    private val dataSource: DataSource,
    private val logger: Logger,
    private val isolationLevel: TransactionIsolationLevel? = null
) : Session {

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
