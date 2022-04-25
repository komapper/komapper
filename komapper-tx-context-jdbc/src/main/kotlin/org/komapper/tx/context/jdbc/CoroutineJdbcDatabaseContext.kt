package org.komapper.tx.context.jdbc

interface CoroutineJdbcDatabaseContext {
    val database: CoroutineAwareJdbcDatabase
}
