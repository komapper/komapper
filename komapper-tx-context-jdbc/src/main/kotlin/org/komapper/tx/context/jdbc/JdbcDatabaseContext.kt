package org.komapper.tx.context.jdbc

import org.komapper.core.ThreadSafe

@ThreadSafe
interface JdbcDatabaseContext {
    val database: ContextualJdbcDatabase
}
