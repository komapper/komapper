package org.komapper.tx.context.jdbc

import org.komapper.tx.jdbc.JdbcTransaction

interface JdbcContext : JdbcDatabaseContext, JdbcTransactionContext

internal class DefaultJdbcContext(
    override val database: ContextAwareJdbcDatabase,
    override val transaction: JdbcTransaction?
) : JdbcContext

fun JdbcContext(
    database: ContextAwareJdbcDatabase,
    transaction: JdbcTransaction? = null
): JdbcContext {
    return DefaultJdbcContext(database, transaction)
}
