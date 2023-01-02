package org.komapper.spring.r2dbc

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.komapper.spring.SpringTransactionDefinition
import org.komapper.tx.core.CoroutineTransactionOperator
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionProperty
import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.Optional
import kotlin.coroutines.coroutineContext

internal class SpringR2dbcCoroutineTransactionOperator(private val transactionManager: ReactiveTransactionManager, private val transaction: ReactiveTransaction? = null) :
    CoroutineTransactionOperator {

    override suspend fun <R> required(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R,
    ): R {
        val definition = SpringTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRED)
        return execute(definition, block)
    }

    override suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R,
    ): R {
        val definition = SpringTransactionDefinition(transactionProperty, TransactionAttribute.REQUIRES_NEW)
        return execute(definition, block)
    }

    private suspend fun <R> execute(
        definition: org.springframework.transaction.TransactionDefinition,
        block: suspend (CoroutineTransactionOperator) -> R,
    ): R {
        val context = coroutineContext
        val txOp = TransactionalOperator.create(transactionManager, definition)
        val flux = txOp.execute { tx ->
            flow {
                val operator = SpringR2dbcCoroutineTransactionOperator(transactionManager, tx)
                try {
                    val value = block(operator)
                    emit(value)
                } finally {
                    if (!tx.isNewTransaction && tx.isRollbackOnly) {
                        // Rollback the enclosing transaction
                        transaction?.setRollbackOnly()
                    }
                }
            }.map { Optional.ofNullable(it) }.asFlux(context)
        }
        return flux.asFlow().map { it.orElse(null) }.single()
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
