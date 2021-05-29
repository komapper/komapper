package org.komapper.tx.jdbc

import java.util.UUID

interface Transaction {
    val connection: TransactionConnection
    var isRollbackOnly: Boolean
    fun isInitialized(): Boolean
}

internal class TransactionImpl(connectionProvider: () -> TransactionConnection) : Transaction {
    private val id = UUID.randomUUID()
    private val connectionDelegate = lazy(connectionProvider)
    override val connection: TransactionConnection by connectionDelegate
    override var isRollbackOnly: Boolean = false
    override fun isInitialized() = connectionDelegate.isInitialized()
    override fun toString() = id.toString()
}
