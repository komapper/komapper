package org.komapper.tx.context.r2dbc

import org.komapper.tx.r2dbc.R2dbcTransaction

interface R2dbcTransactionContext {
    val transaction: R2dbcTransaction?
}

private class DefaultR2dbcTransactionContext(
    override val transaction: R2dbcTransaction
) : R2dbcTransactionContext

object EmptyR2dbcTransactionContext : R2dbcTransactionContext {
    override val transaction: R2dbcTransaction? = null
}

fun R2dbcTransactionContext(transaction: R2dbcTransaction): R2dbcTransactionContext {
    return DefaultR2dbcTransactionContext(transaction)
}
