package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.collect
import org.komapper.core.LoggerFacade
import org.komapper.r2dbc.R2dbcSession
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.FlowTransactionOperator
import org.komapper.tx.core.TransactionProperty

/**
 * Represents a transactional session for R2DBC.
 */
class R2dbcTransactionSession(
    connectionFactory: ConnectionFactory,
    loggerFacade: LoggerFacade,
    transactionProperty: TransactionProperty = EmptyTransactionProperty
) : R2dbcSession {

    private val transactionManager: R2dbcTransactionManager =
        R2dbcTransactionManagerImpl(connectionFactory, loggerFacade)

    override val coroutineTransactionOperator: CoroutineTransactionOperator =
        R2dbcCoroutineTransactionOperator(transactionManager, transactionProperty)

    override val flowTransactionOperator: FlowTransactionOperator =
        R2dbcFlowTransactionOperator(transactionManager, transactionProperty)

    override suspend fun getConnection(): Connection {
        return transactionManager.getConnection()
    }

    override suspend fun releaseConnection(connection: Connection) {
        connection.close().collect { }
    }
}
