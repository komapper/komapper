package org.komapper.spring.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import org.komapper.r2dbc.R2dbcCoroutineTransactionalOperator
import org.komapper.r2dbc.R2dbcFlowTransactionalOperator
import org.komapper.r2dbc.R2dbcSession
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.transaction.ReactiveTransactionManager

class R2dbcTransactionAwareSession(
    transactionManager: ReactiveTransactionManager,
    connectionFactory: ConnectionFactory
) :
    R2dbcSession {

    private val connectionFactoryProxy = when (connectionFactory) {
        is TransactionAwareConnectionFactoryProxy -> connectionFactory
        else -> TransactionAwareConnectionFactoryProxy(connectionFactory)
    }
    override val coroutineTransactionalOperator: R2dbcCoroutineTransactionalOperator =
        CoroutineTransactionalOperatorAdapter(transactionManager)

    override val flowTransactionalOperator: R2dbcFlowTransactionalOperator =
        FlowTransactionalOperatorAdapter(transactionManager)

    override suspend fun getConnection(): Connection {
        return connectionFactoryProxy.create().asFlow().single()
    }
}
