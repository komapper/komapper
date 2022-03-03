package org.komapper.spring.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import org.komapper.r2dbc.R2dbcSession
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.FlowTransactionOperator
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.transaction.ReactiveTransactionManager

class ReactiveTransactionSession(
    transactionManager: ReactiveTransactionManager,
    connectionFactory: ConnectionFactory
) :
    R2dbcSession {

    private val connectionFactoryProxy = when (connectionFactory) {
        is TransactionAwareConnectionFactoryProxy -> connectionFactory
        else -> TransactionAwareConnectionFactoryProxy(connectionFactory)
    }
    override val coroutineTransactionOperator: CoroutineTransactionOperator =
        ReactiveCoroutineTransactionOperator(transactionManager)

    override val flowTransactionOperator: FlowTransactionOperator =
        ReactiveFlowTransactionOperator(transactionManager)

    override suspend fun getConnection(): Connection {
        return connectionFactoryProxy.create().asFlow().single()
    }
}
