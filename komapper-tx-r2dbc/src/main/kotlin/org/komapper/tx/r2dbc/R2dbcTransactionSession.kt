package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.R2dbcSession
import org.reactivestreams.Publisher

/**
 * Represents a transactional session for R2DBC.
 */
class R2dbcTransactionSession(
    private val connectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade,
    private val transactionDefinition: TransactionDefinition? = null
) : R2dbcSession {

    override val connection: Publisher<out Connection>
        get() = transactionManager.connectionFactory.create()

    val userTransaction: R2dbcUserTransaction by lazy {
        R2dbcTransactionScopeImpl(transactionManager, transactionDefinition)
    }

    val transactionManager: TransactionManager by lazy {
        TransactionManagerImpl(connectionFactory, loggerFacade)
    }
}
