package org.komapper.tx.context.r2dbc

import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

@ThreadSafe
interface ContextAwareR2dbcCoroutineTransactionOperator {
    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(R2dbcContext)
    suspend fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(R2dbcContext) (tx: ContextAwareR2dbcCoroutineTransactionOperator) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(R2dbcContext)
    suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(R2dbcContext) (tx: ContextAwareR2dbcCoroutineTransactionOperator) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    context(R2dbcContext)
    suspend fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    context(R2dbcContext)
    suspend fun isRollbackOnly(): Boolean
}

internal class ContextAwareR2dbcCoroutineTransactionOperatorImpl(
    private val transactionManager: ContextAwareR2dbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : ContextAwareR2dbcCoroutineTransactionOperator {

    context(R2dbcContext)
    override suspend fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) (ContextAwareR2dbcCoroutineTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            block(this@R2dbcContext, this@ContextAwareR2dbcCoroutineTransactionOperatorImpl)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(R2dbcContext)
    override suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) (ContextAwareR2dbcCoroutineTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            val transactionContext = transactionManager.suspend()
            val r2dbcContext = R2dbcContext(database, transactionContext.transaction)
            with(r2dbcContext) {
                executeInNewTransaction(transactionProperty, block)
            }.also {
                transactionManager.resume()
            }
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(R2dbcContext)
    private suspend fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) (ContextAwareR2dbcCoroutineTransactionOperator) -> R
    ): R {
        val transactionContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        val r2dbcContext = R2dbcContext(database, transactionContext.transaction)
        return with(r2dbcContext) {
            runCatching {
                block(r2dbcContext, this@ContextAwareR2dbcCoroutineTransactionOperatorImpl)
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

    context(R2dbcContext)
    override suspend fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    context(R2dbcContext)
    override suspend fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
