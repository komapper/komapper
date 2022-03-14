package org.komapper.spring.jdbc

import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition

class PlatformTransactionOperator(private val transactionManager: PlatformTransactionManager) : TransactionOperator {

    override fun <R> required(transactionProperty: TransactionProperty, block: (TransactionOperator) -> R): R {
        val definition = PlatformTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override fun <R> requiresNew(transactionProperty: TransactionProperty, block: (TransactionOperator) -> R): R {
        val definition = PlatformTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private fun <R> execute(
        definition: TransactionDefinition,
        block: (TransactionOperator) -> R
    ): R {
        val status = transactionManager.getTransaction(definition)
        val operator = object : TransactionOperator by this@PlatformTransactionOperator {
            override fun setRollbackOnly() {
                status.setRollbackOnly()
            }

            override fun isRollbackOnly(): Boolean {
                return status.isRollbackOnly
            }
        }
        return if (status.isNewTransaction) {
            runCatching {
                block(operator)
            }.onSuccess {
                if (status.isRollbackOnly) {
                    transactionManager.rollback(status)
                } else {
                    transactionManager.commit(status)
                }
            }.onFailure { cause ->
                runCatching {
                    transactionManager.rollback(status)
                }.onFailure {
                    cause.addSuppressed(it)
                }
            }.getOrThrow()
        } else {
            runCatching {
                block(operator)
            }.onSuccess {
                if (status.isRollbackOnly) {
                    transactionManager.rollback(status)
                }
            }.getOrThrow()
        }
    }

    override fun setRollbackOnly() {
        throw UnsupportedOperationException()
    }

    override fun isRollbackOnly(): Boolean {
        throw UnsupportedOperationException()
    }
}
