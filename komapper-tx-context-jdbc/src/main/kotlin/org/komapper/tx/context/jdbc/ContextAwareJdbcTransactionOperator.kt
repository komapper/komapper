package org.komapper.tx.context.jdbc

import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

interface ContextAwareJdbcTransactionOperator {

    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(JdbcTransactionContext)
    fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: context(JdbcTransactionContext)(tx: ContextAwareJdbcTransactionOperator) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(JdbcTransactionContext)
    fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: context(JdbcTransactionContext)(tx: ContextAwareJdbcTransactionOperator) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    context(JdbcTransactionContext)
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    context(JdbcTransactionContext)
    fun isRollbackOnly(): Boolean
}

internal class ContextAwareJdbcTransactionOperatorImpl(
    private val transactionManager: ContextAwareJdbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : ContextAwareJdbcTransactionOperator {

    context(JdbcTransactionContext)
        override fun <R> required(
        transactionProperty: TransactionProperty,
        block: context(JdbcTransactionContext) (ContextAwareJdbcTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            block(this@JdbcTransactionContext, this@ContextAwareJdbcTransactionOperatorImpl)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(JdbcTransactionContext)
        override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: context(JdbcTransactionContext) (ContextAwareJdbcTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            val txContext = transactionManager.suspend()
            val result = runCatching {
                with(txContext) {
                    executeInNewTransaction(transactionProperty, block)
                }
            }.onSuccess {
                transactionManager.resume()
            }.onFailure { cause ->
                runCatching {
                    transactionManager.resume()
                }.onFailure {
                    cause.addSuppressed(it)
                }
            }
            result.getOrThrow()
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(JdbcTransactionContext)
        private fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: context(JdbcTransactionContext) (ContextAwareJdbcTransactionOperator) -> R
    ): R {
        val txContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        return with(txContext) {
            runCatching {
                block(txContext, this@ContextAwareJdbcTransactionOperatorImpl)
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
    }

    context(JdbcTransactionContext)
    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    context(JdbcTransactionContext)
    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
