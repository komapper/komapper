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
    context(JdbcContext)
    fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: context(JdbcContext)(tx: ContextAwareJdbcTransactionOperator) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(JdbcContext)
    fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: context(JdbcContext)(tx: ContextAwareJdbcTransactionOperator) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    context(JdbcContext)
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    context(JdbcContext)
    fun isRollbackOnly(): Boolean
}

internal class ContextAwareJdbcTransactionOperatorImpl(
    private val transactionManager: ContextAwareJdbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : ContextAwareJdbcTransactionOperator {

    context(JdbcContext)
    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: context(JdbcContext) (ContextAwareJdbcTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            block(this@JdbcContext, this@ContextAwareJdbcTransactionOperatorImpl)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(JdbcContext)
    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: context(JdbcContext) (ContextAwareJdbcTransactionOperator) -> R
    ): R {
        return if (transactionManager.isActive()) {
            val transactionContext = transactionManager.suspend()
            val jdbcContext = JdbcContext(database, transactionContext.transaction)
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

    context(JdbcContext)
    private fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: context(JdbcContext) (ContextAwareJdbcTransactionOperator) -> R
    ): R {
        val transactionContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        val jdbcContext = JdbcContext(database, transactionContext.transaction)
        return with(jdbcContext) {
            runCatching {
                block(jdbcContext, this@ContextAwareJdbcTransactionOperatorImpl)
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

    context(JdbcContext)
    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    context(JdbcContext)
    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
