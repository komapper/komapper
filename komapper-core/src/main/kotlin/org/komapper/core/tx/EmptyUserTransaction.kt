package org.komapper.core.tx

class EmptyUserTransaction : UserTransaction {
    override fun <R> invoke(isolationLevel: TransactionIsolationLevel?, block: TransactionScope.() -> R): R {
        val scope = EmptyTransactionScope()
        return block(scope)
    }

    override fun <R> required(isolationLevel: TransactionIsolationLevel?, block: TransactionScope.() -> R): R {
        val scope = EmptyTransactionScope()
        return block(scope)
    }

    override fun <R> requiresNew(isolationLevel: TransactionIsolationLevel?, block: TransactionScope.() -> R): R {
        val scope = EmptyTransactionScope()
        return block(scope)
    }
}
