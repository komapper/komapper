package org.komapper.quarkus.jdbc

import org.komapper.jdbc.JdbcTransactionDefinition
import org.komapper.jdbc.JdbcTransactionalOperator
import javax.transaction.Status
import javax.transaction.TransactionManager

internal class TransactionManagerOperator(private val transactionManager: TransactionManager) : JdbcTransactionalOperator {

    override fun <R> required(
        transactionDefinition: JdbcTransactionDefinition?,
        block: (JdbcTransactionalOperator) -> R
    ): R {
        return execute(block)
    }

    override fun <R> requiresNew(
        transactionDefinition: JdbcTransactionDefinition?,
        block: (JdbcTransactionalOperator) -> R
    ): R {
        val tx = transactionManager.suspend()
        return runCatching {
            execute(block)
        }.onSuccess {
            transactionManager.resume(tx)
        }.getOrThrow()
    }

    private fun <R> execute(block: (JdbcTransactionalOperator) -> R): R {
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