package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.R2dbcCoroutineTransactionOperator
import org.komapper.r2dbc.R2dbcFlowTransactionOperator
import org.komapper.r2dbc.R2dbcSession

/**
 * Represents a transactional session for R2DBC.
 */
class R2dbcTransactionSession(
    private val connectionFactory: ConnectionFactory,
    private val loggerFacade: LoggerFacade,
    private val transactionDefinition: TransactionDefinition? = null
) : R2dbcSession {

    private val transactionManager: R2dbcTransactionManager by lazy {
        R2dbcTransactionManagerImpl(connectionFactory, loggerFacade)
    }

    override val coroutineTransactionOperator: R2dbcCoroutineTransactionOperator by lazy {
        R2dbcCoroutineTransactionOperatorImpl(transactionManager, transactionDefinition)
    }

    override val flowTransactionOperator: R2dbcFlowTransactionOperator by lazy {
        R2dbcFlowTransactionOperatorImpl(transactionManager, transactionDefinition)
    }

    override suspend fun getConnection(): Connection {
        return transactionManager.getConnection()
    }
}
