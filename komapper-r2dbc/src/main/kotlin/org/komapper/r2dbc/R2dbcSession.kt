package org.komapper.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.collect
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.FlowTransactionOperator

/**
 * Represents a session for R2DBC access.
 */
@ThreadSafe
interface R2dbcSession {

    val coroutineTransactionOperator: CoroutineTransactionOperator

    val flowTransactionOperator: FlowTransactionOperator

    /**
     * Returns a R2DBC connection.
     */
    suspend fun getConnection(): Connection

    /**
     * Releases a R2DBC connection.
     */
    suspend fun releaseConnection(connection: Connection)

    /**
     * Uses a R2DBC connection.
     */
    suspend fun <R> useConnection(block: suspend (Connection) -> R): R {
        val con = getConnection()
        return runCatching {
            block(con)
        }.onSuccess {
            releaseConnection(con)
        }.onFailure { cause ->
            runCatching {
                releaseConnection(con)
            }.onFailure {
                cause.addSuppressed(it)
            }
        }.getOrThrow()
    }
}

class DefaultR2dbcSession(private val connectionFactory: ConnectionFactory) : R2dbcSession {

    override val coroutineTransactionOperator: CoroutineTransactionOperator
        get() = throw UnsupportedOperationException("Use a module that provides transaction management.")

    override val flowTransactionOperator: FlowTransactionOperator
        get() = throw UnsupportedOperationException("Use a module that provides transaction management.")

    override suspend fun getConnection(): Connection {
        return connectionFactory.create().asFlow().single()
    }

    override suspend fun releaseConnection(connection: Connection) {
        connection.close().collect { }
    }
}
