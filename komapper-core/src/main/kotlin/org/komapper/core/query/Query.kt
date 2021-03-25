package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement

interface Query<T> {
    fun run(config: DatabaseConfig): T
    fun toStatement(config: DatabaseConfig): Statement
}
