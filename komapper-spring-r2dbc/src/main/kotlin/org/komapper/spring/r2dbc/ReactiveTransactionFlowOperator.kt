package org.komapper.spring.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.collect
import org.komapper.core.TransactionAttribute
import org.komapper.r2dbc.R2dbcFlowTransactionOperator
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

internal class ReactiveTransactionFlowOperator(private val transactionManager: ReactiveTransactionManager) : R2dbcFlowTransactionOperator {

    override fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(R2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        val definition = adaptTransactionDefinition(transactionDefinition, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend FlowCollector<R>.(R2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        val definition = adaptTransactionDefinition(transactionDefinition, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private fun <R> execute(
        definition: SpringDefinition,
        block: suspend FlowCollector<R>.(R2dbcFlowTransactionOperator) -> Unit
    ): Flow<R> {
        return flow {
            TransactionalOperator.create(transactionManager, definition).execute {
                flow {
                    block(this@ReactiveTransactionFlowOperator)
                }.map { it ?: Null }.asPublisher()
            }.collect {
                val value = if (it == Null) null else it
                @Suppress("UNCHECKED_CAST")
                value as R
                emit(value)
            }
        }
    }

    override suspend fun setRollbackOnly() {
        transactionManager.getReactiveTransaction(null).asFlow().map {
            it.setRollbackOnly()
        }.collect()
    }

    override suspend fun isRollbackOnly(): Boolean {
        return transactionManager.getReactiveTransaction(null).asFlow().map {
            it.isRollbackOnly
        }.single()
    }
}
