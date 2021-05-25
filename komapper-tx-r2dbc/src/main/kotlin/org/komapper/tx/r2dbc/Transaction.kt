package org.komapper.tx.r2dbc

import java.util.UUID

interface Transaction {
    val connection: TransactionConnection
    var isRollbackOnly: Boolean
}

internal class TransactionImpl(override val connection: TransactionConnection) : Transaction {
    private val id = UUID.randomUUID()
    override var isRollbackOnly: Boolean = false
    override fun toString() = id.toString()
}

internal object EmptyTransaction : Transaction {
    override val connection: TransactionConnection
        get() = throw UnsupportedOperationException()
    override var isRollbackOnly: Boolean
        get() = throw UnsupportedOperationException()
        set(_) = throw UnsupportedOperationException()
}
