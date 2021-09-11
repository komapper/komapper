package org.komapper.spring.boot.autoconfigure.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.r2dbc.R2dbcSession
import org.reactivestreams.Publisher
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy

class TransactionAwareSession(connectionFactory: ConnectionFactory) : R2dbcSession {
    private val connectionFactoryProxy = when (connectionFactory) {
        is TransactionAwareConnectionFactoryProxy -> connectionFactory
        else -> TransactionAwareConnectionFactoryProxy(connectionFactory)
    }

    override val connection: Publisher<out io.r2dbc.spi.Connection>
        get() = connectionFactoryProxy.create()
}
