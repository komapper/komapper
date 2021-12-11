package org.komapper.tx.jdbc

import java.util.UUID

interface Transaction {
    val id: UUID
    val connection: TransactionConnection
    var isRollbackOnly: Boolean
    fun isInitialized(): Boolean
}

internal class TransactionImpl(connectionProvider: () -> TransactionConnection) : Transaction {
    override val id: UUID = UUID.randomUUID()
    private val connectionDelegate = lazy(connectionProvider)
    override val connection: TransactionConnection by connectionDelegate
    override var isRollbackOnly: Boolean = false
    override fun isInitialized() = connectionDelegate.isInitialized()
    override fun toString() = id.toString()
}
