package org.komapper.spring.boot.autoconfigure.jdbc

import org.komapper.core.TransactionAttribute
import org.komapper.jdbc.JdbcIsolationLevel
import org.komapper.jdbc.JdbcTransactionalOperator
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

class TransactionTemplateAdapter(private val transactionManager: PlatformTransactionManager) : JdbcTransactionalOperator {

    override fun <R> required(isolationLevel: JdbcIsolationLevel?, block: (JdbcTransactionalOperator) -> R): R {
        val definition = adaptTransactionDefinition(isolationLevel, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override fun <R> requiresNew(isolationLevel: JdbcIsolationLevel?, block: (JdbcTransactionalOperator) -> R): R {
        val definition = adaptTransactionDefinition(isolationLevel, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private fun <R> execute(
        definition: SpringDefinition,
        block: (JdbcTransactionalOperator) -> R
    ): R {
        val template = TransactionTemplate(transactionManager, definition)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return template.execute {
            block(this@TransactionTemplateAdapter)
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
