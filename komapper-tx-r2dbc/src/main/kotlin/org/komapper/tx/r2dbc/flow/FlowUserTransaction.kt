package org.komapper.tx.r2dbc.flow

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import org.komapper.core.ThreadSafe
import org.komapper.tx.r2dbc.R2dbcTransactionAttribute
import org.komapper.tx.r2dbc.R2dbcTransactionManager

/**
 * The transactional [Flow] APIs for R2DBC.
 */
@ThreadSafe
interface FlowUserTransaction {

    /**
     * Builds a transactional [Flow].
     *
     * @param R the return type of the flow
     * @param transactionAttribute the transaction attribute
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the flow
     */
    suspend fun <R> build(
        transactionAttribute: R2dbcTransactionAttribute = R2dbcTransactionAttribute.REQUIRED,
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
    ): Flow<R> {
        return when (transactionAttribute) {
            R2dbcTransactionAttribute.REQUIRED -> required(transactionDefinition, block)
            R2dbcTransactionAttribute.REQUIRES_NEW -> requiresNew(transactionDefinition, block)
        }
    }

    /**
     * Build a transactional [Flow] with [R2dbcTransactionAttribute.REQUIRED].
     *
     * @param R the return type of the flow
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the flow
     */
    suspend fun <R> required(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
    ): Flow<R>

    /**
     * Build a transactional [Flow] with [R2dbcTransactionAttribute.REQUIRES_NEW].
     *
     * @param R the return type of the flow
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the flow
     */
    suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
    ): Flow<R>

    /**
     * Marks the transaction as rollback.
     */
    suspend fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    suspend fun isRollbackOnly(): Boolean
}

internal class FlowUserTransactionImpl(
    private val transactionManager: R2dbcTransactionManager,
    private val defaultTransactionDefinition: TransactionDefinition? = null
) : FlowUserTransaction {

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
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

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
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
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
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

internal class FlowUserTransactionStub : FlowUserTransaction {

    private var isRollbackOnly = false

    override suspend fun setRollbackOnly() {
        isRollbackOnly = true
    }

    override suspend fun isRollbackOnly(): Boolean {
        return isRollbackOnly
    }

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
    ): Flow<R> {
        return flow { block(this@FlowUserTransactionStub) }
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
    ): Flow<R> {
        return flow { block(this@FlowUserTransactionStub) }
    }
}
