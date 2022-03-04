package org.komapper.tx.jdbc

import java.util.UUID

interface JdbcTransaction {
    val id: UUID
    val name: String?
    val connection: JdbcTransactionConnection
    var isRollbackOnly: Boolean
    fun isInitialized(): Boolean
}

internal class JdbcTransactionImpl(
    override val name: String?,
    connectionProvider: () -> JdbcTransactionConnection
) : JdbcTransaction {
    override val id: UUID = UUID.randomUUID()
    private val connectionDelegate = lazy(connectionProvider)
    override val connection: JdbcTransactionConnection by connectionDelegate
    override var isRollbackOnly: Boolean = false
    override fun isInitialized() = connectionDelegate.isInitialized()
    override fun toString() = "JdbcTransaction(id=$id, name=$name)"
}
