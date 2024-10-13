package org.komapper.quarkus.jdbc

import jakarta.transaction.Status
import jakarta.transaction.TransactionManager
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty

internal class QuarkusJdbcTransactionOperator(private val transactionManager: TransactionManager) : TransactionOperator {
    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: (TransactionOperator) -> R,
    ): R {
        return if (transactionManager.isActive()) {
            block(this)
        } else {
            executeInNewTransaction(block)
        }
    }

    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: (TransactionOperator) -> R,
    ): R {
        return if (transactionManager.isActive()) {
            val tx = transactionManager.suspend()
            runCatching {
                executeInNewTransaction(block)
            }.onSuccess {
                transactionManager.resume(tx)
            }.onFailure { cause ->
                runCatching {
                    transactionManager.resume(tx)
                }.onFailure {
                    cause.addSuppressed(it)
                }
            }.getOrThrow()
        } else {
            executeInNewTransaction(block)
        }
    }

    private fun <R> executeInNewTransaction(block: (TransactionOperator) -> R): R {
        transactionManager.begin()
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

private fun TransactionManager.isRollbackOnly(): Boolean {
    return this.status == Status.STATUS_MARKED_ROLLBACK
}

private fun TransactionManager.isActive(): Boolean {
    return this.status == Status.STATUS_ACTIVE
}
