package org.komapper.tx.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import org.komapper.r2dbc.FlowTransaction

internal class FlowUserTransactionImpl(
    private val transactionManager: R2dbcTransactionManager,
    private val defaultTransactionDefinition: TransactionDefinition? = null
) : FlowTransaction {

    override fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransaction) -> Unit
    ): Flow<R> {
        return flow {
            if (transactionManager.isActive()) {
                block(this@FlowUserTransactionImpl)
            } else {
                val value = executeInNewTransaction(transactionDefinition, block)
                emitAll(value)
            }
        }
    }

    override fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransaction) -> Unit
    ): Flow<R> {
        return flow {
            val value = if (transactionManager.isActive()) {
                val txContext = transactionManager.suspend()
                withContext(txContext) {
                    executeInNewTransaction(transactionDefinition, block)
                }.onCompletion {
                    transactionManager.resume()
                }
            } else {
                executeInNewTransaction(transactionDefinition, block)
            }
            emitAll(value)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransaction) -> Unit
    ): Flow<R> {
        val txContext = transactionManager.begin(transactionDefinition ?: defaultTransactionDefinition)
        return flow {
            kotlin.runCatching {
                block(this@FlowUserTransactionImpl)
            }.onSuccess {
                if (transactionManager.isRollbackOnly()) {
                    transactionManager.rollback()
                } else {
                    transactionManager.commit()
                }
            }.onFailure { cause ->
                kotlin.runCatching {
                    transactionManager.rollback()
                }.onFailure {
                    cause.addSuppressed(it)
                }
            }.getOrThrow()
        }.flowOn(txContext)
    }

    override suspend fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    override suspend fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
