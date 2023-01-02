package org.komapper.tx.r2dbc

import java.util.UUID

interface R2dbcTransaction {
    val id: UUID
    val name: String?
    val connection: R2dbcTransactionConnection
    var isRollbackOnly: Boolean
}

private class R2dbcTransactionImpl(
    override val name: String?,
    override val connection: R2dbcTransactionConnection,
) : R2dbcTransaction {
    override val id: UUID = UUID.randomUUID()

    @Volatile
    override var isRollbackOnly: Boolean = false
    override fun toString() = "R2dbcTransaction(id=$id, name=$name)"
}

fun R2dbcTransaction(name: String?, connection: R2dbcTransactionConnection): R2dbcTransaction {
    return R2dbcTransactionImpl(name, connection)
}
