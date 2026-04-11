package org.komapper.spring.jdbc

import org.komapper.spring.SpringTransactionDefinition
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate

class SpringJdbcTransactionOperator(
    private val transactionManager: PlatformTransactionManager,
    private val status: TransactionStatus? = null,
    override val transactionProperty: TransactionProperty = EmptyTransactionProperty,
) : TransactionOperator {
    override fun <R> required(transactionProperty: TransactionProperty, block: (TransactionOperator) -> R): R {
        val definition = SpringTransactionDefinition(this.transactionProperty + transactionProperty, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override fun <R> requiresNew(transactionProperty: TransactionProperty, block: (TransactionOperator) -> R): R {
        val definition = SpringTransactionDefinition(this.transactionProperty + transactionProperty, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private fun <R> execute(
        definition: SpringTransactionDefinition,
        block: (TransactionOperator) -> R,
    ): R {
        val txOp = TransactionTemplate(transactionManager, definition)

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val result: Result<R> = txOp.execute { s ->
            val operator = SpringJdbcTransactionOperator(transactionManager, s, definition.transactionProperty)
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

    override fun isActive(): Boolean {
        if (status == null) {
            return false
        }
        return status.hasTransaction()
    }
}
