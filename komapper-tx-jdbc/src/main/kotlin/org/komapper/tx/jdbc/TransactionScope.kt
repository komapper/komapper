package org.komapper.tx.jdbc

import org.komapper.core.Scope

/**
 * The JDBC transaction scope.
 */
@Scope
interface TransactionScope : UserTransaction {
    /**
     * Marks the transaction as rollback.
     */
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    fun isRollbackOnly(): Boolean
}

internal class TransactionScopeImpl(
    private val transactionManager: TransactionManager,
    private val defaultIsolationLevel: IsolationLevel? = null
) : TransactionScope {

    override fun <R> required(
        isolationLevel: IsolationLevel?,
        block: TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    override fun <R> requiresNew(
        isolationLevel: IsolationLevel?,
        block: TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            val tx = transactionManager.suspend()
            try {
                executeInNewTransaction(isolationLevel, block)
            } finally {
                transactionManager.resume(tx)
            }
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    private fun <R> executeInNewTransaction(
        isolationLevel: IsolationLevel?,
        block: TransactionScope.() -> R
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

    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly
    }
}

internal class TransactionScopeStub : TransactionScope {

    private var isRollbackOnly = false

    override fun setRollbackOnly() {
        isRollbackOnly = true
    }

    override fun isRollbackOnly(): Boolean {
        return isRollbackOnly
    }

    override fun <R> required(isolationLevel: IsolationLevel?, block: TransactionScope.() -> R): R {
        return block(this)
    }

    override fun <R> requiresNew(isolationLevel: IsolationLevel?, block: TransactionScope.() -> R): R {
        return block(this)
    }
}
