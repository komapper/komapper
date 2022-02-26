package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.CoroutineTransaction
import org.komapper.r2dbc.FlowTransaction
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

    override val coroutineTransaction: CoroutineTransaction by lazy {
        CoroutineTransactionImpl(transactionManager, transactionDefinition)
    }

    override val flowTransaction: FlowTransaction by lazy {
        FlowUserTransactionImpl(transactionManager, transactionDefinition)
    }

    override suspend fun getConnection(): Connection {
        return transactionManager.getConnection()
    }
}
