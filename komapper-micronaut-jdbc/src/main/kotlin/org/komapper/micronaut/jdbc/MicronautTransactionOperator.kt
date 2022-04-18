package org.komapper.micronaut.jdbc

import io.micronaut.transaction.TransactionDefinition
import io.micronaut.transaction.TransactionOperations
import io.micronaut.transaction.TransactionStatus
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty

class MicronautTransactionOperator(
    private val operations: TransactionOperations<*>,
    private val status: TransactionStatus<*>? = null
) : TransactionOperator {

    override fun <R> required(transactionProperty: TransactionProperty, block: (TransactionOperator) -> R): R {
        val definition = MicronautTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override fun <R> requiresNew(transactionProperty: TransactionProperty, block: (TransactionOperator) -> R): R {
        val definition = MicronautTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private fun <R> execute(
        definition: TransactionDefinition,
        block: (TransactionOperator) -> R
    ): R {
        val result: Result<R> = operations.execute(definition) { s ->
            val operator = MicronautTransactionOperator(operations, s)
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
