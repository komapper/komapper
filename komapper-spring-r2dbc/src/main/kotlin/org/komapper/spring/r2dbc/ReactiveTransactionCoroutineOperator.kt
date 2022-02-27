package org.komapper.spring.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.komapper.core.TransactionAttribute
import org.komapper.r2dbc.R2dbcCoroutineTransactionOperator
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

internal class ReactiveTransactionCoroutineOperator(private val transactionManager: ReactiveTransactionManager) :
    R2dbcCoroutineTransactionOperator {

    override suspend fun <R> required(
        transactionDefinition: TransactionDefinition?,
        block: suspend (R2dbcCoroutineTransactionOperator) -> R
    ): R {
        val definition = adaptTransactionDefinition(transactionDefinition, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition?,
        block: suspend (R2dbcCoroutineTransactionOperator) -> R
    ): R {
        val definition = adaptTransactionDefinition(transactionDefinition, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private suspend fun <R> execute(
        definition: SpringDefinition,
        block: suspend (R2dbcCoroutineTransactionOperator) -> R
    ): R {
        return TransactionalOperator.create(transactionManager, definition).execute {
            flow<Any?> {
                val value = block(this@ReactiveTransactionCoroutineOperator)
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
