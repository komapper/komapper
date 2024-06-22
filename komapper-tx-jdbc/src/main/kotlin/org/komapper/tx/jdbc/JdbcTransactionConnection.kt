package org.komapper.tx.jdbc

import org.komapper.tx.core.TransactionProperty
import java.sql.Connection

interface JdbcTransactionConnection : Connection {
    fun initialize()
    fun reset()
    fun dispose()
    override fun close()
}

private class JdbcTransactionConnectionImpl(
    private val connection: Connection,
    private val isolationLevelProperty: TransactionProperty.IsolationLevel?,
    private val readOnlyProperty: TransactionProperty.ReadOnly?,
) : Connection by connection, JdbcTransactionConnection {

    @Volatile
    private var isolation: Int? = null

    @Volatile
    private var readOnly: Boolean? = null

    @Volatile
    private var autoCommit: Boolean = false

    override fun initialize() {
        if (readOnlyProperty != null) {
            val currentReadOnly = connection.isReadOnly
            if (readOnlyProperty.value != currentReadOnly) {
                connection.isReadOnly = readOnlyProperty.value
                readOnly = currentReadOnly
            }
        }
        if (isolationLevelProperty != null) {
            val currentIsolationLevel = connection.transactionIsolation
            if (isolationLevelProperty.value != currentIsolationLevel) {
                connection.transactionIsolation = isolationLevelProperty.value
                isolation = currentIsolationLevel
            }
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
        val preservedIsolation = isolation
        if (preservedIsolation != null && preservedIsolation != Connection.TRANSACTION_NONE) {
            connection.transactionIsolation = preservedIsolation
        }
        val preservedReadOnly = readOnly
        if (preservedReadOnly != null) {
            connection.isReadOnly = preservedReadOnly
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
    readOnlyProperty: TransactionProperty.ReadOnly?,
): JdbcTransactionConnection {
    return JdbcTransactionConnectionImpl(connection, isolationLevelProperty, readOnlyProperty)
}
