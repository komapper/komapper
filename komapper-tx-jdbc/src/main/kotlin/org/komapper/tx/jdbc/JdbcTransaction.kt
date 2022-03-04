package org.komapper.tx.jdbc

import java.util.UUID

interface JdbcTransaction {
    val id: UUID
    val name: String?
    val connection: JdbcTransactionConnection
    var isRollbackOnly: Boolean
}

internal class JdbcTransactionImpl(
    override val name: String?,
    override val connection: JdbcTransactionConnection
) : JdbcTransaction {
    override val id: UUID = UUID.randomUUID()
    override var isRollbackOnly: Boolean = false
    override fun toString() = "JdbcTransaction(id=$id, name=$name)"
}
