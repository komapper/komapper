package org.komapper.tx.jdbc

import org.komapper.core.ThreadSafe

/**
 * The JDBC transaction APIs designed to be used in general cases.
 * If the isolationLevel is null, the default isolation level is determined by the driver.
 */
@ThreadSafe
interface JdbcUserTransaction {

    /**
     * Runs a transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param isolationLevel the isolation level.
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> run(
        transactionAttribute: JdbcTransactionAttribute = JdbcTransactionAttribute.REQUIRED,
        isolationLevel: JdbcIsolationLevel? = null,
        block: JdbcTransactionScope.() -> R
    ): R {
        return when (transactionAttribute) {
            JdbcTransactionAttribute.REQUIRED -> required(isolationLevel, block)
            JdbcTransactionAttribute.REQUIRES_NEW -> requiresNew(isolationLevel, block)
        }
    }

    /**
     * Begins a transaction with [JdbcTransactionAttribute.REQUIRED].
     *
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> required(
        isolationLevel: JdbcIsolationLevel? = null,
        block: JdbcTransactionScope.() -> R
    ): R

    /**
     * Begins a transaction with [JdbcTransactionAttribute.REQUIRES_NEW].
     *
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> requiresNew(
        isolationLevel: JdbcIsolationLevel? = null,
        block: JdbcTransactionScope.() -> R
    ): R
}
