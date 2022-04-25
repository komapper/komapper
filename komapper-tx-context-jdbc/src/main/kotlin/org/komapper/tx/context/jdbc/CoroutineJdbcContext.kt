package org.komapper.tx.context.jdbc

import org.komapper.tx.jdbc.JdbcTransaction

interface CoroutineJdbcContext : CoroutineJdbcDatabaseContext, JdbcTransactionContext

internal class CoroutineJdbcContextImpl(
    override val database: CoroutineAwareJdbcDatabase,
    override val transaction: JdbcTransaction?
) : CoroutineJdbcContext

fun CoroutineJdbcContext(
    database: CoroutineAwareJdbcDatabase,
    transaction: JdbcTransaction? = null
): CoroutineJdbcContext {
    return CoroutineJdbcContextImpl(database, transaction)
}
