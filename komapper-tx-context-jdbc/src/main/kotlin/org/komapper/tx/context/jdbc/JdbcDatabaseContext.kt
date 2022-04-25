package org.komapper.tx.context.jdbc

interface JdbcDatabaseContext {
    val database: ContextAwareJdbcDatabase
}
