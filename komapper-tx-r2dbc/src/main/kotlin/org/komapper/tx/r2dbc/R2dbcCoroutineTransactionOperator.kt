package org.komapper.tx.r2dbc

import kotlinx.coroutines.withContext
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

internal class R2dbcCoroutineTransactionOperator(
    private val transactionManager: R2dbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty,
) : CoroutineTransactionOperator {
    override suspend fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R,
    ): R {
        return if (transactionManager.isActive()) {
            block(this)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    override suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R,
    ): R {
        return if (transactionManager.isActive()) {
            val txContext = transactionManager.suspend()
            withContext(txContext) {
                executeInNewTransaction(transactionProperty, block)
            }.also {
                transactionManager.resume()
            }
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R,
    ): R {
        val txContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        return withContext(txContext) {
            runCatching {
                block(this@R2dbcCoroutineTransactionOperator)
            }.onSuccess {
                if (transactionManager.isRollbackOnly()) {
                    transactionManager.rollback()
                } else {
                    transactionManager.commit()
                }
            }.onFailure { cause ->
                runCatching {
                    transactionManager.rollback()
                }.onFailure {
                    cause.addSuppressed(it)
                }
            }.getOrThrow()
        }
    }

    override suspend fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override suspend fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
