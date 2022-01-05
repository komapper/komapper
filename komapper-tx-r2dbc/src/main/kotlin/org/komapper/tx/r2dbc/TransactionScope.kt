package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.withContext
import org.komapper.core.Scope

/**
 * The R2DBC transaction scope.
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

    override suspend fun <R> required(
        isolationLevel: IsolationLevel?,
        block: suspend TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    override suspend fun <R> requiresNew(
        isolationLevel: IsolationLevel?,
        block: suspend TransactionScope.() -> R
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

    private suspend fun <R> executeInNewTransaction(
        isolationLevel: IsolationLevel?,
        block: suspend TransactionScope.() -> R
    ): R {
        val context = transactionManager.begin(isolationLevel ?: defaultIsolationLevel)
        return withContext(context) {
            try {
                val result = block(this@TransactionScopeImpl)
                if (!transactionManager.isRollbackOnly) {
                    transactionManager.commit()
                }
                result
            } finally {
                transactionManager.rollback()
            }
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

    override suspend fun <R> required(isolationLevel: IsolationLevel?, block: suspend TransactionScope.() -> R): R {
        return block(this)
    }

    override suspend fun <R> requiresNew(isolationLevel: IsolationLevel?, block: suspend TransactionScope.() -> R): R {
        return block(this)
    }
}
