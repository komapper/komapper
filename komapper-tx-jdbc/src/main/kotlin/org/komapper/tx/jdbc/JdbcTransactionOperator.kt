package org.komapper.tx.jdbc

import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty

internal class JdbcTransactionOperator(
    private val transactionManager: JdbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : TransactionOperator {

    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: (TransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            block(this)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: (TransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            val tx = transactionManager.suspend()
            val result = runCatching {
                executeInNewTransaction(transactionProperty, block)
            }.onSuccess {
                transactionManager.resume(tx)
            }.onFailure { cause ->
                runCatching {
                    transactionManager.resume(tx)
                }.onFailure {
                    cause.addSuppressed(it)
                }
            }
            result.getOrThrow()
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    private fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: (TransactionOperator) -> R
    ): R {
        transactionManager.begin(defaultTransactionProperty + transactionProperty)
        return runCatching {
            block(this)
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

    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
