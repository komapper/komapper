package org.komapper.spring.r2dbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.asFlux
import org.komapper.tx.core.FlowTransactionOperator
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.Optional
import kotlin.coroutines.coroutineContext

internal class ReactiveFlowTransactionOperator(private val transactionManager: ReactiveTransactionManager) :
    FlowTransactionOperator {

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
            val context = coroutineContext
            val txOp = TransactionalOperator.create(transactionManager, definition)
            val flux = txOp.execute { tx ->
                flow {
                    block(object : FlowTransactionOperator by this@ReactiveFlowTransactionOperator {
                        override suspend fun setRollbackOnly() {
                            tx.setRollbackOnly()
                        }

                        override suspend fun isRollbackOnly(): Boolean {
                            return tx.isRollbackOnly
                        }
                    })
                }.map { Optional.ofNullable(it) }.asFlux(context)
            }
            flux.collect {
                val value = it.orElse(null)
                emit(value)
            }
        }
    }

    override suspend fun setRollbackOnly() {
        throw UnsupportedOperationException()
    }

    override suspend fun isRollbackOnly(): Boolean {
        throw UnsupportedOperationException()
    }
}
