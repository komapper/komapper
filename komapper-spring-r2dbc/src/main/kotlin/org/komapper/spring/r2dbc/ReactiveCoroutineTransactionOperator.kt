package org.komapper.spring.r2dbc

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import kotlin.coroutines.coroutineContext

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
        val context = coroutineContext
        val txOp = TransactionalOperator.create(transactionManager, definition)
        val flux = txOp.execute { tx ->
            flow {
                val value = block(object : CoroutineTransactionOperator by this@ReactiveCoroutineTransactionOperator {
                    override suspend fun setRollbackOnly() {
                        tx.setRollbackOnly()
                    }

                    override suspend fun isRollbackOnly(): Boolean {
                        return tx.isRollbackOnly
                    }
                })
                emit(value)
            }.map { it ?: Null }.asFlux(context)
        }
        val value = flux.asFlow().map { if (it == Null) null else it }.single()
        @Suppress("UNCHECKED_CAST")
        return value as R
    }

    override suspend fun setRollbackOnly() {
        throw UnsupportedOperationException()
    }

    override suspend fun isRollbackOnly(): Boolean {
        throw UnsupportedOperationException()
    }
}
