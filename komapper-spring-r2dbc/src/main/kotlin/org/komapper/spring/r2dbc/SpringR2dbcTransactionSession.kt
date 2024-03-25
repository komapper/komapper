package org.komapper.spring.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.collect
import org.komapper.r2dbc.R2dbcSession
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.FlowTransactionOperator
import org.springframework.r2dbc.connection.ConnectionFactoryUtils
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionContextManager
import reactor.core.publisher.Mono

class SpringR2dbcTransactionSession(
    transactionManager: ReactiveTransactionManager,
    override val connectionFactory: ConnectionFactory,
) :
    R2dbcSession {

    override val coroutineTransactionOperator: CoroutineTransactionOperator =
        SpringR2dbcCoroutineTransactionOperator(transactionManager)

    override val flowTransactionOperator: FlowTransactionOperator =
        SpringR2dbcFlowTransactionOperator(transactionManager)

    override suspend fun getConnection(): Connection {
        return ConnectionFactoryUtils.getConnection(connectionFactory).asFlow().single()
    }

    override suspend fun releaseConnection(connection: Connection) {
        TransactionContextManager.currentContext().flatMap {
            Mono.empty<Void>()
        }.onErrorResume(NoTransactionException::class.java) {
            ConnectionFactoryUtils.releaseConnection(connection, connectionFactory)
        }.asFlow().collect()
    }
}
