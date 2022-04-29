package org.komapper.tx.context.jdbc

import org.komapper.tx.jdbc.JdbcTransaction

interface JdbcContext : JdbcDatabaseContext, JdbcTransactionContext

private class JdbcContextImpl(
    override val database: ContextualJdbcDatabase,
    override val transaction: JdbcTransaction?
) : JdbcContext

internal fun JdbcContext(
    database: ContextualJdbcDatabase,
    transaction: JdbcTransaction? = null
): JdbcContext {
    return JdbcContextImpl(database, transaction)
}
