package org.komapper.core.tx

import org.komapper.core.Scope

@Scope
class TransactionScope(
    private val transactionManager: TransactionManager,
    private val defaultIsolationLevel: TransactionIsolationLevel? = null
) {

    fun <R> required(
        isolationLevel: TransactionIsolationLevel? = defaultIsolationLevel,
        block: TransactionScope.() -> R
    ): R = if (transactionManager.isActive) {
        block(this)
    } else {
        executeInNewTransaction(isolationLevel, block)
    }

    fun <R> requiresNew(
        isolationLevel: TransactionIsolationLevel? = defaultIsolationLevel,
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
        block: TransactionScope.() -> R
    ): R {
        transactionManager.begin(isolationLevel)
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

    fun setRollbackOnly() = transactionManager.setRollbackOnly()

    fun isRollbackOnly(): Boolean = transactionManager.isRollbackOnly
}
