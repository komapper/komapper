package org.komapper.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import org.komapper.core.ThreadSafe

@ThreadSafe
interface R2dbcFlowTransactionalOperator {

    /**
     * Build a REQUIRED transactional [Flow].
     *
     * @param R the return type of the flow
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the flow
     */
    fun <R> required(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(R2dbcFlowTransactionalOperator) -> Unit
    ): Flow<R>

    /**
     * Build a REQUIRES_NEW transactional [Flow].
     *
     * @param R the return type of the flow
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the flow
     */
    fun <R> requiresNew(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend FlowCollector<R>.(R2dbcFlowTransactionalOperator) -> Unit
    ): Flow<R>

    /**
     * Marks the transaction as rollback.
     */
    suspend fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    suspend fun isRollbackOnly(): Boolean
}
