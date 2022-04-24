package org.komapper.tx.context.jdbc

import org.komapper.tx.jdbc.JdbcTransaction

interface JdbcTransactionContext {
    val transaction: JdbcTransaction?
}

internal class DefaultJdbcTransactionContext(
    override val transaction: JdbcTransaction
) : JdbcTransactionContext

internal object EmptyJdbcTransactionContext : JdbcTransactionContext {
    override val transaction: JdbcTransaction? = null
}

fun JdbcTransactionContext(transaction: JdbcTransaction): JdbcTransactionContext {
    return DefaultJdbcTransactionContext(transaction)
}
