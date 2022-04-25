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
    context(CoroutineJdbcContext)
    suspend fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(CoroutineJdbcContext)(tx: CoroutineAwareJdbcTransactionOperator) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(CoroutineJdbcContext)
    suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(CoroutineJdbcContext)(tx: CoroutineAwareJdbcTransactionOperator) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    context(CoroutineJdbcContext)
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    context(CoroutineJdbcContext)
    fun isRollbackOnly(): Boolean
}

internal class CoroutineAwareJdbcTransactionOperatorImpl(
    private val transactionManager: ContextAwareJdbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : CoroutineAwareJdbcTransactionOperator {

    context(CoroutineJdbcContext)
    override suspend fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend context(CoroutineJdbcContext) (CoroutineAwareJdbcTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            block(this@CoroutineJdbcContext, this@CoroutineAwareJdbcTransactionOperatorImpl)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(CoroutineJdbcContext)
    override suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend context(CoroutineJdbcContext) (CoroutineAwareJdbcTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            val transactionContext = transactionManager.suspend()
            val jdbcContext = CoroutineJdbcContext(database, transactionContext.transaction)
            val result = runCatching {
                with(jdbcContext) {
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

    context(CoroutineJdbcContext)
    private suspend fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: suspend context(CoroutineJdbcContext) (CoroutineAwareJdbcTransactionOperator) -> R
    ): R {
        val transactionContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        val jdbcContext = CoroutineJdbcContext(database, transactionContext.transaction)
        return with(jdbcContext) {
            runCatching {
                block(jdbcContext, this@CoroutineAwareJdbcTransactionOperatorImpl)
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

    context(CoroutineJdbcContext)
    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    context(CoroutineJdbcContext)
    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
