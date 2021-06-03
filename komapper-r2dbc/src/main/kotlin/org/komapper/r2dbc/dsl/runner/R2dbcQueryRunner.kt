package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.query.QueryRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

sealed interface R2dbcQueryRunner<T> : QueryRunner {
    suspend fun run(config: R2dbcDatabaseConfig): T
}
