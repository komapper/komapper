package org.komapper.tx.jdbc

import org.komapper.jdbc.JdbcIsolationLevel
import org.komapper.jdbc.ThreadTransaction

internal class ThreadTransactionImpl(
    private val transactionManager: JdbcTransactionManager,
    private val defaultIsolationLevel: JdbcIsolationLevel? = null
) : ThreadTransaction {

    override fun <R> required(
        isolationLevel: JdbcIsolationLevel?,
        block: (ThreadTransaction) -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    override fun <R> requiresNew(
        isolationLevel: JdbcIsolationLevel?,
        block: (ThreadTransaction) -> R
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
        block: (ThreadTransaction) -> R
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
