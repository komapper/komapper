package org.komapper.jdbc

interface JdbcTransactionOperator {

    /**
     * Begins a REQUIRED transaction.
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> required(
        transactionDefinition: JdbcTransactionDefinition? = null,
        block: (JdbcTransactionOperator) -> R
    ): R

    /**
     * Begins a REQUIRES_NEW transaction.
     *
     * @param R the return type of the block
     * @param transactionDefinition the transaction definition
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> requiresNew(
        transactionDefinition: JdbcTransactionDefinition? = null,
        block: (JdbcTransactionOperator) -> R
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
