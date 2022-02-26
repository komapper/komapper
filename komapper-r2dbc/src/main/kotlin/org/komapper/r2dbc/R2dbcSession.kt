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

    val coroutineTransactionalOperator: R2dbcCoroutineTransactionalOperator

    val flowTransactionalOperator: R2dbcFlowTransactionalOperator

    /**
     * Returns a R2DBC connection.
     */
    suspend fun getConnection(): Connection
}

class DefaultR2dbcSession(private val connectionFactory: ConnectionFactory) : R2dbcSession {

    override val coroutineTransactionalOperator: R2dbcCoroutineTransactionalOperator
        get() = throw UnsupportedOperationException("Use a module that provides transaction management.")

    override val flowTransactionalOperator: R2dbcFlowTransactionalOperator
        get() = throw UnsupportedOperationException("Use a module that provides transaction management.")

    override suspend fun getConnection(): Connection {
        return connectionFactory.create().asFlow().single()
    }
}
