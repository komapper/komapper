package org.komapper.tx.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import org.komapper.core.ThreadSafe

@ThreadSafe
interface FlowTransactionOperator {
    /**
     * Build a REQUIRED transactional [Flow].
     *
     * @param R the return type of the flow
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the flow
     */
    fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit,
    ): Flow<R>

    /**
     * Build a REQUIRES_NEW transactional [Flow].
     *
     * @param R the return type of the flow
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the flow
     */
    fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend FlowCollector<R>.(FlowTransactionOperator) -> Unit,
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
