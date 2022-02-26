package org.komapper.jdbc

interface ThreadTransaction {

    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> required(
        isolationLevel: JdbcIsolationLevel? = null,
        block: (ThreadTransaction) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param isolationLevel the isolation level
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> requiresNew(
        isolationLevel: JdbcIsolationLevel? = null,
        block: (ThreadTransaction) -> R
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
