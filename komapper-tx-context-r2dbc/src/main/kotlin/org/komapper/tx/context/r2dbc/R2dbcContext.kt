package org.komapper.tx.context.r2dbc

import org.komapper.tx.r2dbc.R2dbcTransaction

interface R2dbcContext : R2dbcDatabaseContext, R2dbcTransactionContext, R2dbcTransactionOperatorContext

private class R2dbcContextImpl(
    override val database: ContextualR2dbcDatabase,
    override val transactionOperator: ContextualR2dbcCoroutineTransactionOperator,
    override val flowTransactionOperator: ContextualR2dbcFlowTransactionOperator,
    override val transaction: R2dbcTransaction?,
) : R2dbcContext

internal fun R2dbcContext(
    database: ContextualR2dbcDatabase,
    transactionOperator: ContextualR2dbcCoroutineTransactionOperator,
    flowTransactionOperator: ContextualR2dbcFlowTransactionOperator,
    transaction: R2dbcTransaction? = null,
): R2dbcContext {
    return R2dbcContextImpl(database, transactionOperator, flowTransactionOperator, transaction)
}
