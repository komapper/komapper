package org.komapper.tx.r2dbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.FlowTransactionOperator
import org.komapper.tx.core.TransactionProperty

internal class R2dbcFlowTransactionOperator(
    private val transactionManager: R2dbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : FlowTransactionOperator {

    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R> {
        return flow {
            if (transactionManager.isActive()) {
                block(this@R2dbcFlowTransactionOperator)
            } else {
                val value = executeInNewTransaction(transactionProperty, block)
                emitAll(value)
            }
        }
    }

    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R> {
        return flow {
            val value = if (transactionManager.isActive()) {
                val txContext = transactionManager.suspend()
                withContext(txContext) {
                    executeInNewTransaction(transactionProperty, block)
                }.onCompletion {
                    transactionManager.resume()
                }
            } else {
                executeInNewTransaction(transactionProperty, block)
            }
            emitAll(value)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R> {
        val txContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        return flow {
            kotlin.runCatching {
                block(this@R2dbcFlowTransactionOperator)
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
