package org.komapper.tx.core

import org.komapper.core.ThreadSafe

@ThreadSafe
interface TransactionOperator {
    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> required(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: (TransactionOperator) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> requiresNew(
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: (TransactionOperator) -> R
    ): R

    /**
     * Marks the transaction as rollback.
     */
    fun setRollbackOnly()

    /**
     * Returns true if the transaction is marked as rollback.
     */
    fun isRollbackOnly(): Boolean
}
