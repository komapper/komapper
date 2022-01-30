package org.komapper.tx.jdbc

import java.sql.Connection

interface TransactionConnection : Connection {
    fun initialize()
    fun reset()
    fun dispose()
    override fun close()
}

internal class TransactionConnectionImpl(
    private val connection: Connection,
    private val isolationLevel: IsolationLevel?
) : Connection by connection, TransactionConnection {

    private var isolation: Int = 0
    private var autoCommitState: Boolean = false

    override fun initialize() {
        isolation = connection.transactionIsolation
        if (isolationLevel != null) {
            connection.transactionIsolation = isolationLevel.value
        }
        autoCommitState = connection.autoCommit
        if (autoCommitState) {
            connection.autoCommit = false
        }
    }

    override fun reset() {
        kotlin.runCatching {
            if (isolationLevel != null && isolation != Connection.TRANSACTION_NONE) {
                connection.transactionIsolation = isolation
            }
            if (autoCommitState) {
                connection.autoCommit = true
            }
        }
    }

    override fun dispose() {
        runCatching {
            if (!connection.isClosed) {
                connection.close()
            }
        }
    }

    // do nothing
    override fun close() = Unit
}
