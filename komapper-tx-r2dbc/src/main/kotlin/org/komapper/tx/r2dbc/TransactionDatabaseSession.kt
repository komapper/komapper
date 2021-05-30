package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.IsolationLevel
import org.komapper.core.Logger
import org.komapper.r2dbc.R2dbcDatabaseSession
import org.reactivestreams.Publisher

class TransactionDatabaseSession(
    private val connectionFactory: ConnectionFactory,
    private val logger: Logger,
    private val isolationLevel: IsolationLevel? = null
) : R2dbcDatabaseSession {

    override fun getConnection(): Publisher<out Connection> {
        return transactionManager.connectionFactory.create()
    }

    val userTransaction: UserTransaction by lazy {
        TransactionScopeImpl(transactionManager, isolationLevel)
    }

    val transactionManager: TransactionManager by lazy {
        TransactionManagerImpl(connectionFactory, logger)
    }
}
