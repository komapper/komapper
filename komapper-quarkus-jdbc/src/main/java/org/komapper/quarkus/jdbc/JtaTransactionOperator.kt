package org.komapper.quarkus.jdbc

import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import javax.transaction.Status
import javax.transaction.TransactionManager

internal class JtaTransactionOperator(private val transactionManager: TransactionManager) : TransactionOperator {

    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: (TransactionOperator) -> R
    ): R {
        return execute(block)
    }

    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: (TransactionOperator) -> R
    ): R {
        val tx = transactionManager.suspend()
        return runCatching {
            execute(block)
        }.onSuccess {
            transactionManager.resume(tx)
        }.getOrThrow()
    }

    private fun <R> execute(block: (TransactionOperator) -> R): R {
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