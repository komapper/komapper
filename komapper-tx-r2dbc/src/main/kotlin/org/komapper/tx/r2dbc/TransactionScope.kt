package org.komapper.tx.r2dbc

import io.r2dbc.spi.TransactionDefinition
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
    private val defaultTransactionDefinition: TransactionDefinition? = null
) : TransactionScope {

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            val tx = transactionManager.suspend()
            val result = runCatching {
                executeInNewTransaction(transactionDefinition, block)
            }
            transactionManager.resume(tx)
            result.getOrThrow()
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        transactionDefinition: TransactionDefinition?,
        block: suspend TransactionScope.() -> R
    ): R {
        return transactionManager.begin(transactionDefinition ?: defaultTransactionDefinition) {
            runCatching {
                block(this@TransactionScopeImpl)
            }.onFailure {
                kotlin.runCatching {
                    transactionManager.rollback()
                }
            }.onSuccess {
                if (transactionManager.isRollbackOnly) {
                    transactionManager.rollback()
                } else {
                    transactionManager.commit()
                }
            }.getOrThrow()
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

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend TransactionScope.() -> R
    ): R {
        return block(this)
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend TransactionScope.() -> R
    ): R {
        return block(this)
    }
}
