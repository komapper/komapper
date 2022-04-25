package org.komapper.tx.context.r2dbc

import org.komapper.tx.r2dbc.R2dbcTransaction

interface R2dbcContext : R2dbcDatabaseContext, R2dbcTransactionContext

private class DefaultR2dbcContext(
    override val database: ContextAwareR2dbcDatabase,
    override val transaction: R2dbcTransaction?
) : R2dbcContext

fun R2dbcContext(
    database: ContextAwareR2dbcDatabase,
    transaction: R2dbcTransaction? = null
): R2dbcContext {
    return DefaultR2dbcContext(database, transaction)
}
