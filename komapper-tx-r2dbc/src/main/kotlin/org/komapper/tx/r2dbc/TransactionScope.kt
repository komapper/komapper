package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import kotlinx.coroutines.withContext
import org.komapper.core.Scope

@Scope
interface TransactionScope : UserTransaction {
    fun setRollbackOnly()
    fun isRollbackOnly(): Boolean
}

internal class TransactionScopeImpl(
    private val transactionManager: TransactionManager,
    private val defaultIsolationLevel: IsolationLevel? = null
) : TransactionScope {

    override suspend fun <R> required(
        isolationLevel: IsolationLevel?,
        block: suspend TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    override suspend fun <R> requiresNew(
        isolationLevel: IsolationLevel?,
        block: suspend TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            val context = transactionManager.createEmptyContext()
            withContext(context) {
                executeInNewTransaction(isolationLevel, block)
            }
        } else {
            executeInNewTransaction(isolationLevel, block)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        isolationLevel: IsolationLevel?,
        block: suspend TransactionScope.() -> R
    ): R {
        val context = transactionManager.begin(isolationLevel ?: defaultIsolationLevel)
        return withContext(context) {
            try {
                val result = block(this@TransactionScopeImpl)
                if (!transactionManager.isRollbackOnly) {
                    transactionManager.commit()
                }
                result
            } finally {
                transactionManager.rollback()
            }
        }
    }

    override fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly
    }
}
