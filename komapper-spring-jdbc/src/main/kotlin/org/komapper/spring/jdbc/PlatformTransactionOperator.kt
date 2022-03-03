package org.komapper.spring.jdbc

import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

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
        val template = TransactionTemplate(transactionManager, definition)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return template.execute {
            block(this@PlatformTransactionOperator)
        }
    }

    override fun setRollbackOnly() {
        val status = transactionManager.getTransaction(null)
        status.setRollbackOnly()
    }

    override fun isRollbackOnly(): Boolean {
        val status = transactionManager.getTransaction(null)
        return status.isRollbackOnly
    }
}
