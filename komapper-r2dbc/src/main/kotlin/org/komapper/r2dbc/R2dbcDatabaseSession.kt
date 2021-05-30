package org.komapper.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import org.komapper.core.ThreadSafe
import org.reactivestreams.Publisher

@ThreadSafe
interface R2dbcDatabaseSession {
    fun getConnection(): Publisher<out Connection>
}

class DefaultR2dbcDatabaseSession(private val connectionFactory: ConnectionFactory) : R2dbcDatabaseSession {
    override fun getConnection(): Publisher<out Connection> {
        return connectionFactory.create()
    }
}
