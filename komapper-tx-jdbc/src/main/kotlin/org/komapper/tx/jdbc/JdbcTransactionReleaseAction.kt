package org.komapper.tx.jdbc

fun interface JdbcTransactionReleaseAction {
    fun execute()
}
