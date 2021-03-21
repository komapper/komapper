package org.komapper.core.tx

class TransactionScopeInitiator(
    private val transactionManager: TransactionManager,
    private val defaultIsolationLevel: TransactionIsolationLevel? = null
) {

    operator fun <R> invoke(
        isolationLevel: TransactionIsolationLevel? = defaultIsolationLevel,
        block: TransactionScope.() -> R
    ) = required(isolationLevel, block)

    fun <R> required(
        isolationLevel: TransactionIsolationLevel? = defaultIsolationLevel,
        block: TransactionScope.() -> R
    ): R {
        val scope = TransactionScope(transactionManager, defaultIsolationLevel)
        return scope.required(isolationLevel, block)
    }

    fun <R> requiresNew(
        isolationLevel: TransactionIsolationLevel? = defaultIsolationLevel,
        block: TransactionScope.() -> R
    ): R {
        val scope = TransactionScope(transactionManager, defaultIsolationLevel)
        return scope.requiresNew(isolationLevel, block)
    }
}
