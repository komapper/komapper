package org.komapper.core.tx

class DefaultUserTransaction(
    private val transactionManager: TransactionManager,
    private val defaultIsolationLevel: TransactionIsolationLevel? = null
) : UserTransaction {

    override operator fun <R> invoke(
        isolationLevel: TransactionIsolationLevel?,
        block: TransactionScope.() -> R
    ) = required(isolationLevel, block)

    override fun <R> required(
        isolationLevel: TransactionIsolationLevel?,
        block: TransactionScope.() -> R
    ): R {
        val scope = DefaultTransactionScope(transactionManager, defaultIsolationLevel)
        return scope.required(isolationLevel, block)
    }

    override fun <R> requiresNew(
        isolationLevel: TransactionIsolationLevel?,
        block: TransactionScope.() -> R
    ): R {
        val scope = DefaultTransactionScope(transactionManager, defaultIsolationLevel)
        return scope.requiresNew(isolationLevel, block)
    }
}
