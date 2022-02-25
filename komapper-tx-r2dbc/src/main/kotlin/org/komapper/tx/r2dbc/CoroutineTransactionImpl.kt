package org.komapper.tx.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.komapper.r2dbc.CoroutineTransaction

internal class CoroutineTransactionImpl(
    private val transactionManager: R2dbcTransactionManager,
    private val defaultTransactionDefinition: TransactionDefinition? = null
) : CoroutineTransaction {

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend CoroutineScope.(CoroutineTransaction) -> R
    ): R {
        return if (transactionManager.isActive()) {
            coroutineScope {
                block(this@CoroutineTransactionImpl)
            }
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend CoroutineScope.(CoroutineTransaction) -> R
    ): R {
        return if (transactionManager.isActive()) {
            val txContext = transactionManager.suspend()
            withContext(txContext) {
                executeInNewTransaction(transactionDefinition, block)
            }.also {
                transactionManager.resume()
            }
        } else {
            executeInNewTransaction(transactionDefinition, block)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        transactionDefinition: TransactionDefinition?,
        block: suspend CoroutineScope.(CoroutineTransaction) -> R
    ): R {
        val txContext = transactionManager.begin(transactionDefinition ?: defaultTransactionDefinition)
        return withContext(txContext) {
            runCatching {
                block(this@CoroutineTransactionImpl)
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

    override suspend fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override suspend fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
