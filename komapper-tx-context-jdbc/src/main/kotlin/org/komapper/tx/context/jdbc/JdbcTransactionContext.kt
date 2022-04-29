package org.komapper.tx.context.jdbc

import org.komapper.tx.jdbc.JdbcTransaction

interface JdbcTransactionContext {
    val transaction: JdbcTransaction?
}

private class DefaultJdbcTransactionContext(
    override val transaction: JdbcTransaction
) : JdbcTransactionContext

internal object EmptyJdbcTransactionContext : JdbcTransactionContext {
    override val transaction: JdbcTransaction? = null
}

internal fun JdbcTransactionContext(transaction: JdbcTransaction): JdbcTransactionContext {
    return DefaultJdbcTransactionContext(transaction)
}
