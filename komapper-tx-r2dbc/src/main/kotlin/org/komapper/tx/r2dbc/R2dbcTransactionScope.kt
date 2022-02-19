package org.komapper.tx.r2dbc

import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.Scope

/**
 * The R2DBC transaction scope.
 */
@Scope
interface R2dbcTransactionScope : R2dbcUserTransaction {
    /**
     * Marks the transaction as rollback.
     */
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    fun isRollbackOnly(): Boolean
}

internal class R2dbcTransactionScopeImpl(
    private val transactionManager: TransactionManager,
    private val defaultTransactionDefinition: TransactionDefinition? = null
) : R2dbcTransactionScope {

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend R2dbcTransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend R2dbcTransactionScope.() -> R
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
        block: suspend R2dbcTransactionScope.() -> R
    ): R {
        return transactionManager.begin(transactionDefinition ?: defaultTransactionDefinition) {
            runCatching {
                block(this@R2dbcTransactionScopeImpl)
            }.onFailure { cause ->
                runCatching {
                    transactionManager.rollback()
                }.onFailure {
                    cause.addSuppressed(it)
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

internal class R2dbcTransactionScopeStub : R2dbcTransactionScope {

    private var isRollbackOnly = false

    override fun setRollbackOnly() {
        isRollbackOnly = true
    }

    override fun isRollbackOnly(): Boolean {
        return isRollbackOnly
    }

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend R2dbcTransactionScope.() -> R
    ): R {
        return block(this)
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend R2dbcTransactionScope.() -> R
    ): R {
        return block(this)
    }
}
