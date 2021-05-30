package org.komapper.tx.jdbc

import java.sql.Connection
import java.sql.SQLException

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
        if (isolationLevel != null && isolation != Connection.TRANSACTION_NONE) {
            connection.transactionIsolation = isolation
        }
        if (autoCommitState) {
            connection.autoCommit = true
        }
    }

    override fun dispose() {
        try {
            connection.close()
        } catch (ignored: SQLException) {
        }
    }

    // do nothing
    override fun close() = Unit
}
