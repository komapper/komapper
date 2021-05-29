package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.reactive.awaitFirst
import org.komapper.core.Logger
import org.komapper.r2dbc.R2dbcDatabaseSession

class TransactionDatabaseSession(
    override val connectionFactory: ConnectionFactory,
    private val logger: Logger,
    private val isolationLevel: IsolationLevel? = null
) : R2dbcDatabaseSession {

    override suspend fun getConnection(): Connection {
        return transactionManager.connectionFactory.create().awaitFirst()
    }

    val userTransaction: UserTransaction by lazy {
        TransactionScopeImpl(transactionManager, isolationLevel)
    }

    val transactionManager: TransactionManager by lazy {
        TransactionManagerImpl(connectionFactory, logger)
    }
}
