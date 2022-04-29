package org.komapper.tx.context.r2dbc

import org.komapper.tx.r2dbc.R2dbcTransaction

interface R2dbcContext : R2dbcDatabaseContext, R2dbcTransactionContext

private class R2dbcContextImpl(
    override val database: ContextualR2dbcDatabase,
    override val transaction: R2dbcTransaction?
) : R2dbcContext

internal fun R2dbcContext(
    database: ContextualR2dbcDatabase,
    transaction: R2dbcTransaction? = null
): R2dbcContext {
    return R2dbcContextImpl(database, transaction)
}
