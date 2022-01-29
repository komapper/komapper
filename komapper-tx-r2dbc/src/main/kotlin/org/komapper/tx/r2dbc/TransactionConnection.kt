package org.komapper.tx.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.reactivestreams.Publisher

interface TransactionConnection : Connection {
    suspend fun initialize()
    suspend fun reset()
    suspend fun dispose()
    override fun close(): Publisher<Void>
}

internal class TransactionConnectionImpl(
    private val connection: Connection,
    private val isolationLevel: IsolationLevel?
) : Connection by connection, TransactionConnection {

    private val preservedIsolationLevel: IsolationLevel? = connection.transactionIsolationLevel
    private val preservedAutoCommitState: Boolean = connection.isAutoCommit

    override suspend fun initialize() {
        // if (isolationLevel != null) {
        //     connection.setTransactionIsolationLevel(isolationLevel).awaitFirstOrNull()
        // }
        // if (preservedAutoCommitState) {
        //     connection.setAutoCommit(false).awaitFirstOrNull()
        // }
    }

    override suspend fun reset() {
        // if (preservedIsolationLevel != null && isolationLevel != null) {
        //     connection.setTransactionIsolationLevel(preservedIsolationLevel).awaitFirstOrNull()
        // }
        // if (preservedAutoCommitState) {
        //     connection.setAutoCommit(true).awaitFirstOrNull()
        // }
    }

    override suspend fun dispose() {
        connection.close().awaitFirstOrNull()
    }

    override fun close(): Publisher<Void> {
        return emptyFlow<Void>().asPublisher()
    }
}
