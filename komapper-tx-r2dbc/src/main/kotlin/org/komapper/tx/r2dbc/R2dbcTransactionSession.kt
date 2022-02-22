package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.R2dbcSession
import org.komapper.tx.r2dbc.flow.FlowUserTransaction
import org.komapper.tx.r2dbc.flow.FlowUserTransactionImpl

/**
 * Represents a transactional session for R2DBC.
 */
class R2dbcTransactionSession(
    private val connectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade,
    private val transactionDefinition: TransactionDefinition? = null
) : R2dbcSession {

    override suspend fun getConnection(): Connection {
        return transactionManager.getConnection()
    }

    val userTransaction: R2dbcUserTransaction by lazy {
        R2dbcUserTransactionImpl(transactionManager, transactionDefinition)
    }

    val flowUserTransaction: FlowUserTransaction by lazy {
        FlowUserTransactionImpl(transactionManager, transactionDefinition)
    }

    val transactionManager: R2dbcTransactionManager by lazy {
        R2dbcTransactionManagerImpl(connectionFactory, loggerFacade)
    }
}
