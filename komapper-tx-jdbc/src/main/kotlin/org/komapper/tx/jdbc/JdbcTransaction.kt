package org.komapper.tx.jdbc

import java.util.UUID

interface JdbcTransaction {
    val id: UUID
    val connection: JdbcTransactionConnection
    var isRollbackOnly: Boolean
    fun isInitialized(): Boolean
}

internal class JdbcTransactionImpl(connectionProvider: () -> JdbcTransactionConnection) : JdbcTransaction {
    override val id: UUID = UUID.randomUUID()
    private val connectionDelegate = lazy(connectionProvider)
    override val connection: JdbcTransactionConnection by connectionDelegate
    override var isRollbackOnly: Boolean = false
    override fun isInitialized() = connectionDelegate.isInitialized()
    override fun toString() = id.toString()
}
