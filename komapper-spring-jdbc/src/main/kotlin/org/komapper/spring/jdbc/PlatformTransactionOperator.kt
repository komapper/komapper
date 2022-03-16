package org.komapper.spring.jdbc

import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate

class PlatformTransactionOperator(
    private val transactionManager: PlatformTransactionManager,
    private val status: TransactionStatus? = null
) : TransactionOperator {

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
        val txOp = TransactionTemplate(transactionManager, definition)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val result: Result<R> = txOp.execute { s ->
            val operator = PlatformTransactionOperator(transactionManager, s)
            runCatching {
                block(operator)
            }.onSuccess {
                if (!s.isNewTransaction && s.isRollbackOnly) {
                    // Rollback the enclosing transaction
                    status?.setRollbackOnly()
                }
            }.onFailure {
                if (!s.isNewTransaction && s.isRollbackOnly) {
                    // Rollback the enclosing transaction
                    status?.setRollbackOnly()
                }
                if (s.isNewTransaction) {
                    // Rollback the current transaction
                    s.setRollbackOnly()
                }
            }
        }
        return result.getOrThrow()
    }

    override fun setRollbackOnly() {
        if (status == null) {
            error("The transaction is null.")
        }
        return status.setRollbackOnly()
    }

    override fun isRollbackOnly(): Boolean {
        if (status == null) {
            error("The status is null.")
        }
        return status.isRollbackOnly
    }
}
