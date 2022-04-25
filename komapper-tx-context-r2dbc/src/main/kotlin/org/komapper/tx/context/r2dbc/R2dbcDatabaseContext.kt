package org.komapper.tx.context.r2dbc

interface R2dbcDatabaseContext {
    val database: ContextAwareR2dbcDatabase
}
