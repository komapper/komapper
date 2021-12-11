package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.IsolationLevel
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.R2dbcSession
import org.reactivestreams.Publisher

class TransactionSession(
    private val connectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade,
    private val isolationLevel: IsolationLevel? = null
) : R2dbcSession {

    override val connection: Publisher<out Connection>
        get() = transactionManager.connectionFactory.create()

    val userTransaction: UserTransaction by lazy {
        TransactionScopeImpl(transactionManager, isolationLevel)
    }

    val transactionManager: TransactionManager by lazy {
        TransactionManagerImpl(connectionFactory, loggerFacade)
    }
}
