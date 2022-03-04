package org.komapper.tx.r2dbc

import java.util.UUID

interface R2dbcTransaction {
    val id: UUID
    val name: String?
    val connection: R2dbcTransactionConnection
    var isRollbackOnly: Boolean
}

internal class R2dbcTransactionImpl(
    override val name: String?,
    override val connection: R2dbcTransactionConnection
) : R2dbcTransaction {
    override val id: UUID = UUID.randomUUID()
    override var isRollbackOnly: Boolean = false
    override fun toString() = "R2dbcTransaction(id=$id, name=$name)"
}
