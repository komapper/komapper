package org.komapper.jdbc

data class JdbcTransactionReadOnly(val value: Boolean) : JdbcTransactionDefinition.Element {
    companion object Key : JdbcTransactionDefinition.Key<JdbcTransactionReadOnly>
    override val key: JdbcTransactionDefinition.Key<JdbcTransactionReadOnly> = Key
}
