package org.komapper.r2dbc

import io.r2dbc.spi.TransactionDefinition
import kotlinx.coroutines.CoroutineScope
import org.komapper.core.ThreadSafe

@ThreadSafe
interface CoroutineTransaction {

    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> required(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend CoroutineScope.(CoroutineTransaction) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend CoroutineScope.(CoroutineTransaction) -> R
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
