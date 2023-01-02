package org.komapper.tx.jdbc

import java.util.UUID

interface JdbcTransaction {
    val id: UUID
    val name: String?
    val connection: JdbcTransactionConnection
    var isRollbackOnly: Boolean
}

private class JdbcTransactionImpl(
    override val name: String?,
    override val connection: JdbcTransactionConnection,
) : JdbcTransaction {
    override val id: UUID = UUID.randomUUID()

    @Volatile
    override var isRollbackOnly: Boolean = false
    override fun toString() = "JdbcTransaction(id=$id, name=$name)"
}

fun JdbcTransaction(
    name: String?,
    connection: JdbcTransactionConnection,
): JdbcTransaction {
    return JdbcTransactionImpl(name, connection)
}
