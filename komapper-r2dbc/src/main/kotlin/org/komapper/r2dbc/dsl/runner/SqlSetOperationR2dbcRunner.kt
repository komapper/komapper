package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect

internal class SqlSetOperationR2dbcRunner<T, R>(
    context: SqlSetOperationContext<T>,
    options: SqlSetOperationOptions,
    transform: (R2dbcDialect, Row) -> T,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcRunner<R> {

    private val flowBuilder: SqlSetOperationFlowBuilder<T> = SqlSetOperationFlowBuilder(context, options, transform)

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val flow = flowBuilder.build(config)
        return collect(flow)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return flowBuilder.dryRun(config)
    }
}
