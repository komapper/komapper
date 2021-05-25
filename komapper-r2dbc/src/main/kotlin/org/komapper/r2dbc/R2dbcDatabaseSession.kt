package org.komapper.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitFirst
import org.komapper.core.ThreadSafe

@ThreadSafe
interface R2dbcDatabaseSession {
    val connectionFactory: ConnectionFactory
    // TODO
    suspend fun getConnection(): Connection
}

class DefaultR2dbcDatabaseSession(override val connectionFactory: ConnectionFactory) : R2dbcDatabaseSession {
    override suspend fun getConnection(): Connection {
        return connectionFactory.create().awaitFirst()
    }
}
