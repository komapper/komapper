package org.komapper.tx.jdbc

import org.komapper.jdbc.JdbcTransactionDefinition
import org.komapper.jdbc.JdbcTransactionalOperator

internal class JdbcTransactionalOperatorImpl(
    private val transactionManager: JdbcTransactionManager,
    private val defaultTransactionDefinition: JdbcTransactionDefinition? = null
) : JdbcTransactionalOperator {

    override fun <R> required(
        transactionDefinition: JdbcTransactionDefinition?,
        block: (JdbcTransactionalOperator) -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    override fun <R> requiresNew(
        transactionDefinition: JdbcTransactionDefinition?,
        block: (JdbcTransactionalOperator) -> R
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

    private fun <R> executeInNewTransaction(
        transactionDefinition: JdbcTransactionDefinition?,
        block: (JdbcTransactionalOperator) -> R
    ): R {
        transactionManager.begin(transactionDefinition ?: defaultTransactionDefinition)
        return runCatching {
            block(this)
        }.onSuccess {
            if (transactionManager.isRollbackOnly) {
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
        return transactionManager.isRollbackOnly
    }
}
