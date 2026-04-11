package org.komapper.tx.r2dbc

import kotlinx.coroutines.withContext
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

internal class R2dbcCoroutineTransactionOperator(
    private val transactionManager: R2dbcTransactionManager,
    override val transactionProperty: TransactionProperty = EmptyTransactionProperty,
) : CoroutineTransactionOperator {
    override suspend fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R,
    ): R {
        return if (transactionManager.isActive()) {
            val operator = R2dbcCoroutineTransactionOperator(transactionManager, this.transactionProperty + transactionProperty)
            block(operator)
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
        val newTransactionProperty = this.transactionProperty + transactionProperty
        val txContext = transactionManager.begin(newTransactionProperty)
        return withContext(txContext) {
            runCatching {
                val operator = R2dbcCoroutineTransactionOperator(transactionManager, newTransactionProperty)
                block(operator)
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

    override suspend fun isActive(): Boolean {
        return transactionManager.isActive()
    }
}
