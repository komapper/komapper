package org.komapper.spring.r2dbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.komapper.tx.core.FlowTransactionOperator
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.Optional
import kotlin.coroutines.coroutineContext

internal class ReactiveFlowTransactionOperator(private val transactionManager: ReactiveTransactionManager, private val transaction: ReactiveTransaction? = null) :
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
                    val operator = ReactiveFlowTransactionOperator(transactionManager, tx)
                    block(operator)
                    if (!tx.isNewTransaction && tx.isRollbackOnly) {
                        // Rollback the enclosing transaction
                        transaction?.setRollbackOnly()
                    }
                }.map { Optional.ofNullable(it) }.asFlux(context)
            }
            val flow = flux.asFlow().map { it.orElse(null) }
            emitAll(flow)
        }
    }

    override suspend fun setRollbackOnly() {
        if (transaction == null) {
            error("The transaction is null.")
        }
        transaction.setRollbackOnly()
    }

    override suspend fun isRollbackOnly(): Boolean {
        if (transaction == null) {
            error("The transaction is null.")
        }
        return transaction.isRollbackOnly
    }
}
