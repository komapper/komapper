package org.komapper.core.tx

interface Transaction {
    val connection: TransactionConnection
    var isRollbackOnly: Boolean
    fun isInitialized(): Boolean
}

internal class TransactionImpl(connectionProvider: () -> TransactionConnection) : Transaction {
    private val id = System.identityHashCode(this).toString()
    private val connectionDelegate = lazy(connectionProvider)
    override val connection: TransactionConnection by connectionDelegate
    override var isRollbackOnly: Boolean = false
    override fun isInitialized() = connectionDelegate.isInitialized()
    override fun toString() = id
}
