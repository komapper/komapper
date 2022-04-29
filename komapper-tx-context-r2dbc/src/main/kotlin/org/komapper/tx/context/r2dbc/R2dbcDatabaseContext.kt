package org.komapper.tx.context.r2dbc

import org.komapper.core.ThreadSafe

@ThreadSafe
interface R2dbcDatabaseContext {
    val database: ContextualR2dbcDatabase
}
