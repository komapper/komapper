package org.komapper.core.tx

import org.komapper.core.Scope

@Scope
class EmptyTransactionScope : TransactionScope {
    override fun <R> required(isolationLevel: TransactionIsolationLevel?, block: TransactionScope.() -> R): R {
        return block(this)
    }

    override fun <R> requiresNew(isolationLevel: TransactionIsolationLevel?, block: TransactionScope.() -> R): R {
        return block(this)
    }

    override fun setRollbackOnly() {
    }

    override fun isRollbackOnly(): Boolean {
        return false
    }
}
