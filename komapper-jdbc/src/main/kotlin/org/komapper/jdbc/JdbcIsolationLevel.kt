package org.komapper.jdbc

import java.sql.Connection

enum class JdbcIsolationLevel constructor(val value: Int) : JdbcTransactionDefinition.Element {
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    companion object Key : JdbcTransactionDefinition.Key<JdbcIsolationLevel>

    override val key: JdbcTransactionDefinition.Key<JdbcIsolationLevel> get() = Key
}
