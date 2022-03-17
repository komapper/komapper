package org.komapper.spring.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.collect
import org.komapper.r2dbc.R2dbcSession
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.FlowTransactionOperator
import org.springframework.r2dbc.connection.ConnectionFactoryUtils
import org.springframework.transaction.ReactiveTransactionManager

class ReactiveTransactionSession(
    transactionManager: ReactiveTransactionManager,
    private val connectionFactory: ConnectionFactory
) :
    R2dbcSession {

    override val coroutineTransactionOperator: CoroutineTransactionOperator =
        ReactiveCoroutineTransactionOperator(transactionManager)

    override val flowTransactionOperator: FlowTransactionOperator =
        ReactiveFlowTransactionOperator(transactionManager)

    override suspend fun getConnection(): Connection {
        return ConnectionFactoryUtils.getConnection(connectionFactory).asFlow().single()
    }

    override suspend fun releaseConnection(connection: Connection) {
        // TODO: Remove "isAutoCommit" check in the future.
        // This is a workaround to avoid Spring's IllegalTransactionStateException
        // See https://github.com/spring-projects/spring-framework/issues/28133
        if (connection.isAutoCommit) {
            ConnectionFactoryUtils.releaseConnection(connection, connectionFactory).collect { }
        }
    }
}
