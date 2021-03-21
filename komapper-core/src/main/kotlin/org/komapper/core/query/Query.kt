package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig

interface Query<T> {
    fun run(config: DefaultDatabaseConfig): T
    fun toSql(config: DefaultDatabaseConfig): String
}
