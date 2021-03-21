package org.komapper.core.tx

import java.sql.Connection
import java.sql.SQLException

class TransactionConnection(
    private val connection: Connection,
    private val isolationLevel: TransactionIsolationLevel?
) : Connection by connection {

    private var isolation: Int = 0
    private var autoCommitState: Boolean = false

    fun initialize() {
        isolation = connection.transactionIsolation
        if (isolationLevel != null) {
            connection.transactionIsolation = isolationLevel.value
        }
        autoCommitState = connection.autoCommit
        if (autoCommitState) {
            connection.autoCommit = false
        }
    }

    fun reset() {
        if (isolationLevel != null && isolation != Connection.TRANSACTION_NONE) {
            connection.transactionIsolation = isolation
        }
        if (autoCommitState) {
            connection.autoCommit = true
        }
    }

    fun dispose() = try {
        connection.close()
    } catch (ignored: SQLException) {
    }

    // do nothing
    override fun close() = Unit
}
