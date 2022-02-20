package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.ThreadSafe
import org.komapper.r2dbc.R2dbcDatabaseConfig

@ThreadSafe
internal sealed interface R2dbcFlowBuilder<T> {
    fun check(config: DatabaseConfig)
    suspend fun build(config: R2dbcDatabaseConfig): Flow<T>
    fun dryRun(config: DatabaseConfig): DryRunStatement
}
