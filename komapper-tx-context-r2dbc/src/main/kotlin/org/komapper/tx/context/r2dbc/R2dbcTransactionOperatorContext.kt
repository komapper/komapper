package org.komapper.tx.context.r2dbc

interface R2dbcTransactionOperatorContext {
    val transactionOperator: ContextualR2dbcCoroutineTransactionOperator
    val flowTransactionOperator: ContextualR2dbcFlowTransactionOperator
}
