package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.query.FlowQueryRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

sealed interface R2dbcFlowQueryRunner<T> : FlowQueryRunner {
    fun run(config: R2dbcDatabaseConfig): Flow<T>
    fun dryRun(config: R2dbcDatabaseConfig): String
}
