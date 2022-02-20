package org.komapper.tx.r2dbc.flow

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.komapper.core.ThreadSafe
import org.komapper.tx.r2dbc.R2dbcTransactionAttribute

/**
 * The R2DBC transaction APIs designed to be used in general cases.
 * If the isolationLevel null, the default isolation level is determined by the driver.
 */
@ThreadSafe
interface FlowUserTransaction {

    /**
     * Runs a transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> run(
        transactionAttribute: R2dbcTransactionAttribute = R2dbcTransactionAttribute.REQUIRED,
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R> {
        return flow {
            val value = when (transactionAttribute) {
                R2dbcTransactionAttribute.REQUIRED -> required(transactionDefinition, block)
                R2dbcTransactionAttribute.REQUIRES_NEW -> requiresNew(transactionDefinition, block)
            }
            emitAll(value)
        }
    }

    /**
     * Begins a transaction with [R2dbcTransactionAttribute.REQUIRED].
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> required(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R>

    /**
     * Begins a transaction with [R2dbcTransactionAttribute.REQUIRES_NEW].
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(FlowTransactionScope) -> Unit
    ): Flow<R>
}
