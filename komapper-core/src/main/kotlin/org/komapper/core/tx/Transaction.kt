package org.komapper.core.tx

class Transaction(connectionProvider: () -> TransactionConnection) {
    private val id = System.identityHashCode(this).toString()
    private val connectionDelegate = lazy(connectionProvider)
    internal val connection: TransactionConnection by connectionDelegate
    internal var isRollbackOnly: Boolean = false
    internal fun isInitialized() = connectionDelegate.isInitialized()
    override fun toString() = id
}
