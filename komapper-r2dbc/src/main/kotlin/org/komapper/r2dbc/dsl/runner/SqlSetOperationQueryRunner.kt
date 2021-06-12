package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect

internal class SqlSetOperationQueryRunner<T, R>(
    context: SqlSetOperationContext<T>,
    option: SqlSetOperationOption,
    transform: (R2dbcDialect, Row) -> T,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcQueryRunner<R> {

    private val runner: SqlSetOperationFlowQueryRunner<T> = SqlSetOperationFlowQueryRunner(context, option, transform)

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val flow = runner.run(config)
        return collect(flow)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return runner.dryRun(config)
    }
}
