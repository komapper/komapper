package org.komapper.tx.core

import org.komapper.core.ThreadSafe

@ThreadSafe
interface CoroutineTransactionOperator {
    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: suspend (CoroutineTransactionOperator) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    suspend fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    suspend fun isRollbackOnly(): Boolean
}
