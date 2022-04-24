package org.komapper.tx.jdbc

import org.komapper.tx.core.TransactionProperty
import java.sql.Connection

interface JdbcTransactionConnection : Connection {
    fun initialize()
    fun reset()
    fun dispose()
    override fun close()
}

internal class JdbcTransactionConnectionImpl(
    private val connection: Connection,
    private val isolationLevelProperty: TransactionProperty.IsolationLevel?,
    private val readOnlyProperty: TransactionProperty.ReadOnly?
) : Connection by connection, JdbcTransactionConnection {

    @Volatile
    private var isolation: Int = 0
    @Volatile
    private var readOnly: Boolean = false
    @Volatile
    private var autoCommit: Boolean = false

    override fun initialize() {
        readOnly = connection.isReadOnly
        if (readOnlyProperty != null) {
            connection.isReadOnly = readOnlyProperty.value
        }
        isolation = connection.transactionIsolation
        if (isolationLevelProperty != null) {
            connection.transactionIsolation = isolationLevelProperty.value
        }
        autoCommit = connection.autoCommit
        if (autoCommit) {
            connection.autoCommit = false
        }
    }

    override fun reset() {
        if (autoCommit) {
            connection.autoCommit = true
        }
        if (isolationLevelProperty != null && isolation != Connection.TRANSACTION_NONE) {
            connection.transactionIsolation = isolation
        }
        if (readOnlyProperty != null) {
            connection.isReadOnly = readOnly
        }
    }

    override fun dispose() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    // do nothing
    override fun close() = Unit
}

fun JdbcTransactionConnection(
    connection: Connection,
    isolationLevelProperty: TransactionProperty.IsolationLevel?,
    readOnlyProperty: TransactionProperty.ReadOnly?
): JdbcTransactionConnection {
    return JdbcTransactionConnectionImpl(connection, isolationLevelProperty, readOnlyProperty)
}