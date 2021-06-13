package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect

internal class R2dbcSqlSelectQueryRunner<T, R>(
    context: SqlSelectContext<*, *, *>,
    options: SqlSelectOptions,
    transform: (R2dbcDialect, Row) -> T,
    private val collect: suspend (Flow<T>) -> R
) :
    R2dbcQueryRunner<R> {

    private val runner: R2dbcSqlSelectFlowQueryRunner<T> = R2dbcSqlSelectFlowQueryRunner(context, options, transform)

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val flow = runner.run(config)
        return collect(flow)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
