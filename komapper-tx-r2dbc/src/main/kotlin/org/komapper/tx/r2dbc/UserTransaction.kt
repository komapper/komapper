package org.komapper.tx.r2dbc

import io.r2dbc.spi.IsolationLevel
import org.komapper.core.ThreadSafe

/**
 * The R2DBC transaction APIs designed to be used in general cases.
 * If the isolationLevel null, the default isolation level is determined by the driver.
 */
@ThreadSafe
interface UserTransaction {

    /**
     * Begins a transaction.
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> withTransaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R {
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> required(isolationLevel, block)
            TransactionAttribute.REQUIRES_NEW -> requiresNew(isolationLevel, block)
        }
    }

    /**
     * Begins a transaction with [TransactionAttribute.REQUIRED].
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> required(
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R

    /**
     * Begins a transaction with [TransactionAttribute.REQUIRES_NEW].
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> requiresNew(
        isolationLevel: IsolationLevel? = null,
        block: suspend TransactionScope.() -> R
    ): R
}
