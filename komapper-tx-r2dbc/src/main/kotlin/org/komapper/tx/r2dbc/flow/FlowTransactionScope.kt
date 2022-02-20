package org.komapper.tx.r2dbc.flow

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import org.komapper.core.Scope
import org.komapper.tx.r2dbc.R2dbcTransactionManager

/**
 * The R2DBC transaction scope.
 */
@Scope
interface FlowTransactionScope : FlowUserTransaction {
    /**
     * Marks the transaction as rollback.
     */
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    fun isRollbackOnly(): Boolean
}

internal class FlowTransactionScopeImpl(
    private val transactionManager: R2dbcTransactionManager,
    private val defaultTransactionDefinition: TransactionDefinition? = null
) : FlowTransactionScope {

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R> {
        return flow {
            if (transactionManager.isActive) {
                block(this@FlowTransactionScopeImpl)
            } else {
                val value = executeInNewTransaction(transactionDefinition, block)
                emitAll(value)
            }
        }
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R> {
        return flow {
            val value = if (transactionManager.isActive) {
                val txContext = transactionManager.suspend()
                withContext(txContext) {
                    executeInNewTransaction(transactionDefinition, block)
                }
            } else {
                executeInNewTransaction(transactionDefinition, block)
            }
            emitAll(value)
        }
    }

    private suspend fun <R> executeInNewTransaction(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R> {
        val txContext = transactionManager.begin(transactionDefinition ?: defaultTransactionDefinition)
        return flow { block(this@FlowTransactionScopeImpl) }
            .flowOn(txContext)
            .onCompletion { cause ->
                withContext(txContext) {
                    if (cause == null) {
                        if (transactionManager.isRollbackOnly) {
                            transactionManager.rollback()
                        } else {
                            transactionManager.commit()
                        }
                    } else {
                        runCatching {
                            transactionManager.rollback()
                        }.onFailure {
                            cause.addSuppressed(it)
                        }
                    }
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

internal class FlowTransactionScopeStub : FlowTransactionScope {

    private var isRollbackOnly = false

    override fun setRollbackOnly() {
        isRollbackOnly = true
    }

    override fun isRollbackOnly(): Boolean {
        return isRollbackOnly
    }

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R> {
        return flow { block(this@FlowTransactionScopeStub) }
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R> {
        return flow { block(this@FlowTransactionScopeStub) }
    }
}
