package org.komapper.core.tx

import org.komapper.core.Scope

@Scope
class DefaultTransactionScope(
    private val transactionManager: TransactionManager,
    private val defaultIsolationLevel: TransactionIsolationLevel? = null
) : TransactionScope {

    override fun <R> required(
        isolationLevel: TransactionIsolationLevel?,
        block: TransactionScope.() -> R
    ): R = if (transactionManager.isActive) {
        block(this)
    } else {
        executeInNewTransaction(isolationLevel, block)
    }

    override fun <R> requiresNew(
        isolationLevel: TransactionIsolationLevel?,
        block: TransactionScope.() -> R
    ): R = if (transactionManager.isActive) {
        val context = transactionManager.suspend()
        try {
            executeInNewTransaction(isolationLevel, block)
        } finally {
            transactionManager.resume(context)
        }
    } else {
        executeInNewTransaction(isolationLevel, block)
    }

    private fun <R> executeInNewTransaction(
        isolationLevel: TransactionIsolationLevel?,
        block: DefaultTransactionScope.() -> R
    ): R {
        transactionManager.begin(isolationLevel ?: defaultIsolationLevel)
        try {
            val result = block(this)
            if (!transactionManager.isRollbackOnly) {
                transactionManager.commit()
            }
            return result
        } finally {
            transactionManager.rollback()
        }
    }

    override fun setRollbackOnly() = transactionManager.setRollbackOnly()

    override fun isRollbackOnly(): Boolean = transactionManager.isRollbackOnly
}
