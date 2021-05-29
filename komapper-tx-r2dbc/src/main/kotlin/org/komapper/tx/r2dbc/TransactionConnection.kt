package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.reactivestreams.Publisher

interface TransactionConnection : Connection {
    fun reset()
    suspend fun dispose()
    override fun close(): Publisher<Void>
}

internal class TransactionConnectionImpl(
    private val connection: Connection,
    private val isolationLevel: IsolationLevel?
) : Connection by connection, TransactionConnection {

    private val isolation: IsolationLevel? = connection.transactionIsolationLevel
    private val autoCommitState: Boolean = connection.isAutoCommit

    init {
        if (isolationLevel != null) {
            connection.transactionIsolationLevel = isolationLevel
        }
        if (autoCommitState) {
            connection.isAutoCommit = false
        }
    }

    override fun reset() {
        if (isolationLevel != null) {
            connection.transactionIsolationLevel = isolation
        }
        if (autoCommitState) {
            connection.isAutoCommit = true
        }
    }

    override suspend fun dispose() {
        connection.close().awaitFirstOrNull()
    }

    override fun close(): Publisher<Void> {
        return emptyFlow<Void>().asPublisher()
    }
}
