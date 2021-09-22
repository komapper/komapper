package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.runner.FlowBuilder
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal sealed interface R2dbcFlowBuilder<T> : FlowBuilder {
    fun run(config: R2dbcDatabaseConfig): Flow<T>
}
