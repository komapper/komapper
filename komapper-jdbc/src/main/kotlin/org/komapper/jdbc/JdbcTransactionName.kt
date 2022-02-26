package org.komapper.jdbc

data class JdbcTransactionName(val value: String) : JdbcTransactionDefinition.Element {
    companion object Key : JdbcTransactionDefinition.Key<JdbcTransactionName>
    override val key: JdbcTransactionDefinition.Key<JdbcTransactionName> = Key
}
