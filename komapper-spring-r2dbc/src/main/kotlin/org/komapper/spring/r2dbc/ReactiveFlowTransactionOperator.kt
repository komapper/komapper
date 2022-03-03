package org.komapper.spring.r2dbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.collect
import org.komapper.tx.core.FlowTransactionOperator
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

internal class ReactiveFlowTransactionOperator(private val transactionManager: ReactiveTransactionManager) : FlowTransactionOperator {

    override fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R> {
        val definition = ReactiveTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R> {
        val definition = ReactiveTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private fun <R> execute(
        definition: org.springframework.transaction.TransactionDefinition,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit
    ): Flow<R> {
        return flow {
            TransactionalOperator.create(transactionManager, definition).execute {
                flow {
                    block(this@ReactiveFlowTransactionOperator)
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
