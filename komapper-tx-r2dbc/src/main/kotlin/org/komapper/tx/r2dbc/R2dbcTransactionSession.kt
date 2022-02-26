package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.R2dbcCoroutineTransactionalOperator
import org.komapper.r2dbc.R2dbcFlowTransactionalOperator
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

    override val coroutineTransactionalOperator: R2dbcCoroutineTransactionalOperator by lazy {
        R2dbcCoroutineTransactionalOperatorImpl(transactionManager, transactionDefinition)
    }

    override val flowTransactionalOperator: R2dbcFlowTransactionalOperator by lazy {
        R2dbcFlowTransactionalOperatorImpl(transactionManager, transactionDefinition)
    }

    override suspend fun getConnection(): Connection {
        return transactionManager.getConnection()
    }
}
