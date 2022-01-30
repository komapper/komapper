package org.komapper.tx.r2dbc

import io.r2dbc.spi.TransactionDefinition
import org.komapper.core.ThreadSafe

/**
 * The R2DBC transaction APIs designed to be used in general cases.
 * If the isolationLevel null, the default isolation level is determined by the driver.
 */
@ThreadSafe
interface UserTransaction {

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
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        transactionDefinition: TransactionDefinition? = null,
        block: suspend TransactionScope.() -> R
    ): R {
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> required(transactionDefinition, block)
            TransactionAttribute.REQUIRES_NEW -> requiresNew(transactionDefinition, block)
        }
    }

    /**
     * Begins a transaction with [TransactionAttribute.REQUIRED].
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> required(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend TransactionScope.() -> R
    ): R

    /**
     * Begins a transaction with [TransactionAttribute.REQUIRES_NEW].
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    suspend fun <R> requiresNew(
        transactionDefinition: TransactionDefinition? = null,
        block: suspend TransactionScope.() -> R
    ): R
}
