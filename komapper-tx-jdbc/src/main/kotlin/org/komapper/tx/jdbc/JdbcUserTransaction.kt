package org.komapper.tx.jdbc

import org.komapper.core.ThreadSafe

/**
 * The JDBC transaction APIs designed to be used in general cases.
 * If the isolationLevel is null, the default isolation level is determined by the driver.
 */
@ThreadSafe
interface JdbcUserTransaction {

    /**
     * Runs a transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param isolationLevel the isolation level.
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> run(
        transactionAttribute: JdbcTransactionAttribute = JdbcTransactionAttribute.REQUIRED,
        isolationLevel: JdbcIsolationLevel? = null,
        block: (JdbcUserTransaction) -> R
    ): R {
        return when (transactionAttribute) {
            JdbcTransactionAttribute.REQUIRED -> required(isolationLevel, block)
            JdbcTransactionAttribute.REQUIRES_NEW -> requiresNew(isolationLevel, block)
        }
    }

    /**
     * Begins a transaction with [JdbcTransactionAttribute.REQUIRED].
     *
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> required(
        isolationLevel: JdbcIsolationLevel? = null,
        block: (JdbcUserTransaction) -> R
    ): R

    /**
     * Begins a transaction with [JdbcTransactionAttribute.REQUIRES_NEW].
     *
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> requiresNew(
        isolationLevel: JdbcIsolationLevel? = null,
        block: (JdbcUserTransaction) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    fun isRollbackOnly(): Boolean
}

internal class JdbcUserTransactionImpl(
    private val transactionManager: JdbcTransactionManager,
    private val defaultIsolationLevel: JdbcIsolationLevel? = null
) : JdbcUserTransaction {

    override fun <R> required(
        isolationLevel: JdbcIsolationLevel?,
        block: (JdbcUserTransaction) -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    override fun <R> requiresNew(
        isolationLevel: JdbcIsolationLevel?,
        block: (JdbcUserTransaction) -> R
    ): R {
        return if (transactionManager.isActive) {
            val tx = transactionManager.suspend()
            val result = runCatching {
                executeInNewTransaction(isolationLevel, block)
            }
            transactionManager.resume(tx)
            result.getOrThrow()
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    private fun <R> executeInNewTransaction(
        isolationLevel: JdbcIsolationLevel?,
        block: (JdbcUserTransaction) -> R
    ): R {
        transactionManager.begin(isolationLevel ?: defaultIsolationLevel)
        return runCatching {
            block(this)
        }.onSuccess {
            if (transactionManager.isRollbackOnly) {
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

    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly
    }
}

internal class JdbcUserTransactionStub : JdbcUserTransaction {

    private var isRollbackOnly = false

    override fun setRollbackOnly() {
        isRollbackOnly = true
    }

    override fun isRollbackOnly(): Boolean {
        return isRollbackOnly
    }

    override fun <R> required(isolationLevel: JdbcIsolationLevel?, block: (JdbcUserTransaction) -> R): R {
        return block(this)
    }

    override fun <R> requiresNew(isolationLevel: JdbcIsolationLevel?, block: (JdbcUserTransaction) -> R): R {
        return block(this)
    }
}
