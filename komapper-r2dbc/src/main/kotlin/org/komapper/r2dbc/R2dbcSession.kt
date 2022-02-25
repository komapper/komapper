package org.komapper.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import org.komapper.core.ThreadSafe

/**
 * Represents a session for R2DBC access.
 */
@ThreadSafe
interface R2dbcSession {

    val coroutineTransaction: CoroutineTransaction

    val flowTransaction: FlowTransaction

/**
     * Returns a R2DBC connection.
     */
    suspend fun getConnection(): Connection
}

class DefaultR2dbcSession(private val connectionFactory: ConnectionFactory) : R2dbcSession {

    override val coroutineTransaction: CoroutineTransaction
        get() = TODO("Not yet implemented")

    override val flowTransaction: FlowTransaction
        get() = TODO("Not yet implemented")

    override suspend fun getConnection(): Connection {
        return connectionFactory.create().asFlow().single()
    }
}
