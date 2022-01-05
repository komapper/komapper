package org.komapper.tx.r2dbc

import java.util.UUID

interface Transaction {
    val id: UUID
    val connection: TransactionConnection
    var isRollbackOnly: Boolean
}

internal class TransactionImpl(override val connection: TransactionConnection) : Transaction {
    override val id: UUID = UUID.randomUUID()
    override var isRollbackOnly: Boolean = false
    override fun toString() = id.toString()
}
