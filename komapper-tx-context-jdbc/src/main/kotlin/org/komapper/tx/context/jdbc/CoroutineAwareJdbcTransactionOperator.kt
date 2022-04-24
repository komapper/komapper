package org.komapper.tx.context.jdbc

import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

interface CoroutineAwareJdbcTransactionOperator {

    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(JdbcTransactionContext)
        suspend fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(JdbcTransactionContext)(tx: CoroutineAwareJdbcTransactionOperator) -> R
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
        suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(JdbcTransactionContext)(tx: CoroutineAwareJdbcTransactionOperator) -> R
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

internal class CoroutineAwareJdbcTransactionOperatorImpl(
    private val transactionManager: ContextAwareJdbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : CoroutineAwareJdbcTransactionOperator {

    context(JdbcTransactionContext)
        override suspend fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend context(JdbcTransactionContext) (CoroutineAwareJdbcTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            block(this@JdbcTransactionContext, this@CoroutineAwareJdbcTransactionOperatorImpl)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(JdbcTransactionContext)
        override suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend context(JdbcTransactionContext) (CoroutineAwareJdbcTransactionOperator) -> R
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
        private suspend fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: suspend context(JdbcTransactionContext) (CoroutineAwareJdbcTransactionOperator) -> R
    ): R {
        val txContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        return with(txContext) {
            runCatching {
                block(txContext, this@CoroutineAwareJdbcTransactionOperatorImpl)
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
