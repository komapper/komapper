package org.komapper.tx.context.jdbc

import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

@ThreadSafe
interface ContextualJdbcTransactionOperator {
    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(jdbcContext: JdbcContext)
    fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: context(JdbcContext)
        () -> R,
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    context(jdbcContext: JdbcContext)
    fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: context(JdbcContext)
        () -> R,
    ): R

    /**
     * Marks the transaction as rollback.
     */
    context(jdbcContext: JdbcContext)
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    context(jdbcContext: JdbcContext)
    fun isRollbackOnly(): Boolean
}

internal class ContextualJdbcTransactionOperatorImpl(
    private val transactionManager: ContextualJdbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty,
) : ContextualJdbcTransactionOperator {
    context(jdbcContext: JdbcContext)
    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: context(JdbcContext)
        () -> R,
    ): R {
        return if (transactionManager.isActive()) {
            block(jdbcContext)
        } else {
            executeInNewTransaction(transactionProperty, block)
        }
    }

    context(jdbcContext: JdbcContext)
    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: context(JdbcContext)
        () -> R,
    ): R {
        return if (transactionManager.isActive()) {
            val transactionContext = transactionManager.suspend()
            val jdbcContext = JdbcContext(jdbcContext.database, jdbcContext.transactionOperator, transactionContext.transaction)
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

    context(jdbcContext: JdbcContext)
    private fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: context(JdbcContext)
        () -> R,
    ): R {
        val transactionContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        val jdbcContext = JdbcContext(jdbcContext.database, jdbcContext.transactionOperator, transactionContext.transaction)
        return with(jdbcContext) {
            runCatching {
                block(this@with)
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

    context(jdbcContext: JdbcContext)
    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    context(jdbcContext: JdbcContext)
    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
