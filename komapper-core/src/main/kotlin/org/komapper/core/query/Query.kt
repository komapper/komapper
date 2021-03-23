package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement

interface Query<T> {
    fun run(config: DefaultDatabaseConfig): T
    fun toStatement(config: DefaultDatabaseConfig): Statement
}
