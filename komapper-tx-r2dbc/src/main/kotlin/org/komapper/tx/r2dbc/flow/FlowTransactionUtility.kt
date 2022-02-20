package org.komapper.tx.r2dbc.flow

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.komapper.r2dbc.R2dbc
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.r2dbc.R2dbcTransactionAttribute
import org.komapper.tx.r2dbc.R2dbcTransactionSession

/**
 * Begins a R2DBC transaction.
 *
 * @param R the return type of the block
 * @param transactionAttribute the transaction attribute
 * @param transactionDefinition the transactionDefinition level
 * @param block the block executed in the transaction
 * @return the result of the block
 */
suspend fun <R> R2dbc.flowTransaction(
    transactionAttribute: R2dbcTransactionAttribute = R2dbcTransactionAttribute.REQUIRED,
    transactionDefinition: TransactionDefinition? = null,
    block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit
): Flow<R> {
    return if (this is R2dbcDatabase) {
        val session = this.config.session
        return if (session is R2dbcTransactionSession) {
            session.flowUserTransaction.run(transactionAttribute, transactionDefinition, block)
        } else {
            withoutTransaction(block)
        }
    } else {
        withoutTransaction(block)
    }
}

private suspend fun <R> withoutTransaction(block: suspend FlowCollector<R>.(FlowUserTransaction) -> Unit): Flow<R> {
    val transactionScope = FlowUserTransactionStub()
    return flow { block(transactionScope) }
}
