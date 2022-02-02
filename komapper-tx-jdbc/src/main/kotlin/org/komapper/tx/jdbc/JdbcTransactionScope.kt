package org.komapper.tx.jdbc

import org.komapper.core.Scope

/**
 * The JDBC transaction scope.
 */
@Scope
interface JdbcTransactionScope : JdbcUserTransaction {
    /**
     * Marks the transaction as rollback.
     */
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    fun isRollbackOnly(): Boolean
}

internal class JdbcTransactionScopeImpl(
    private val transactionManager: JdbcTransactionManager,
    private val defaultIsolationLevel: JdbcIsolationLevel? = null
) : JdbcTransactionScope {

    override fun <R> required(
        isolationLevel: JdbcIsolationLevel?,
        block: JdbcTransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    override fun <R> requiresNew(
        isolationLevel: JdbcIsolationLevel?,
        block: JdbcTransactionScope.() -> R
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
        block: JdbcTransactionScope.() -> R
    ): R {
        transactionManager.begin(isolationLevel ?: defaultIsolationLevel)
        return runCatching {
            block(this)
        }.onFailure {
            runCatching {
                transactionManager.rollback()
            }
        }.onSuccess {
            if (transactionManager.isRollbackOnly) {
                transactionManager.rollback()
            } else {
                transactionManager.commit()
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

internal class JdbcTransactionScopeStub : JdbcTransactionScope {

    private var isRollbackOnly = false

    override fun setRollbackOnly() {
        isRollbackOnly = true
    }

    override fun isRollbackOnly(): Boolean {
        return isRollbackOnly
    }

    override fun <R> required(isolationLevel: JdbcIsolationLevel?, block: JdbcTransactionScope.() -> R): R {
        return block(this)
    }

    override fun <R> requiresNew(isolationLevel: JdbcIsolationLevel?, block: JdbcTransactionScope.() -> R): R {
        return block(this)
    }
}
