package org.komapper.tx.context.jdbc

interface JdbcTransactionOperatorContext {
    val transactionOperator: ContextualJdbcTransactionOperator
}
