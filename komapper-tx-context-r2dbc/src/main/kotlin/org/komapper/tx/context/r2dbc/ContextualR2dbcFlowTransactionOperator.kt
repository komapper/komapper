package org.komapper.tx.context.r2dbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import org.komapper.core.ThreadSafe
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionProperty

@ThreadSafe
interface ContextualR2dbcFlowTransactionOperator {

    context(R2dbcContext)
    fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(R2dbcContext) FlowCollector<R>.(ContextualR2dbcFlowTransactionOperator) -> Unit
    ): Flow<R>

    context(R2dbcContext)
    fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend context(R2dbcContext) FlowCollector<R>.(ContextualR2dbcFlowTransactionOperator) -> Unit
    ): Flow<R>

    context(R2dbcContext)
    suspend fun setRollbackOnly()

    context(R2dbcContext)
    suspend fun isRollbackOnly(): Boolean
}

internal class ContextualR2dbcFlowTransactionOperatorImpl(
    private val transactionManager: ContextualR2dbcTransactionManager,
    private val defaultTransactionProperty: TransactionProperty = EmptyTransactionProperty
) : ContextualR2dbcFlowTransactionOperator {

    context(R2dbcContext)
    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) FlowCollector<R>.(ContextualR2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        return flow {
            if (transactionManager.isActive()) {
                block(this@R2dbcContext, this@flow, this@ContextualR2dbcFlowTransactionOperatorImpl)
            } else {
                val value = executeInNewTransaction(transactionProperty, block)
                emitAll(value)
            }
        }
    }

    context(R2dbcContext)
    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) FlowCollector<R>.(ContextualR2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        return flow {
            val value = if (transactionManager.isActive()) {
                val transactionContext = transactionManager.suspend()
                val r2dbcContext = R2dbcContext(database, transactionContext.transaction)
                with(r2dbcContext) {
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

    context(R2dbcContext)
    private suspend fun <R> executeInNewTransaction(
        transactionProperty: TransactionProperty,
        block: suspend context(R2dbcContext) FlowCollector<R>.(ContextualR2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        val transactionContext = transactionManager.begin(defaultTransactionProperty + transactionProperty)
        val r2dbcContext = R2dbcContext(database, transactionContext.transaction)
        return flow {
            with(r2dbcContext) {
                kotlin.runCatching {
                    block(this@with, this@flow, this@ContextualR2dbcFlowTransactionOperatorImpl)
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
            }
        }
    }

    context(R2dbcContext)
    override suspend fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    context(R2dbcContext)
    override suspend fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly()
    }
}
