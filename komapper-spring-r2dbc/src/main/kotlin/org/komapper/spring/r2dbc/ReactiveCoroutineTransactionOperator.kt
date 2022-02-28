package org.komapper.spring.r2dbc

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.komapper.core.TransactionAttribute
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

internal class ReactiveCoroutineTransactionOperator(private val transactionManager: ReactiveTransactionManager) :
    CoroutineTransactionOperator {

    override suspend fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R
    ): R {
        val definition = ReactiveTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R
    ): R {
        val definition = ReactiveTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private suspend fun <R> execute(
        definition: org.springframework.transaction.TransactionDefinition,
        block: suspend (CoroutineTransactionOperator) -> R
    ): R {
        return TransactionalOperator.create(transactionManager, definition).execute {
            flow<Any?> {
                val value = block(this@ReactiveCoroutineTransactionOperator)
                emit(value)
            }.map { it ?: Null }.asPublisher()
        }.asFlow().map {
            val value = if (it == Null) null else it
            @Suppress("UNCHECKED_CAST")
            value as R
        }.single()
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
