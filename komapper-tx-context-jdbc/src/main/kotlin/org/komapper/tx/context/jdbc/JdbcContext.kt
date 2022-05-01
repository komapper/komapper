package org.komapper.tx.context.jdbc

import org.komapper.tx.jdbc.JdbcTransaction

interface JdbcContext : JdbcDatabaseContext, JdbcTransactionContext, JdbcTransactionOperatorContext

private class JdbcContextImpl(
    override val database: ContextualJdbcDatabase,
    override val transactionOperator: ContextualJdbcTransactionOperator,
    override val transaction: JdbcTransaction?
) : JdbcContext

internal fun JdbcContext(
    database: ContextualJdbcDatabase,
    operator: ContextualJdbcTransactionOperator,
    transaction: JdbcTransaction? = null
): JdbcContext {
    return JdbcContextImpl(database, operator, transaction)
}
