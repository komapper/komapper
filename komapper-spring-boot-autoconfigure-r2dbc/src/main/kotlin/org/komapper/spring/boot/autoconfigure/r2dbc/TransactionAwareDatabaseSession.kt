package org.komapper.spring.boot.autoconfigure.r2dbc

import io.r2dbc.spi.ConnectionFactory
import org.komapper.r2dbc.R2dbcDatabaseSession
import org.reactivestreams.Publisher
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy

class TransactionAwareDatabaseSession(connectionFactory: ConnectionFactory) : R2dbcDatabaseSession {
    private val connectionFactoryProxy = when (connectionFactory) {
        is TransactionAwareConnectionFactoryProxy -> connectionFactory
        else -> TransactionAwareConnectionFactoryProxy(connectionFactory)
    }

    override fun getConnection(): Publisher<out io.r2dbc.spi.Connection> {
        return connectionFactoryProxy.create()
    }
}
