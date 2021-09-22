package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.TemplateSelectRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class TemplateSelectR2dbcRunner<T, R>(
    sql: String,
    data: Any,
    private val transform: (Row) -> T,
    private val options: TemplateSelectOptions,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcRunner<R> {

    private val runner = TemplateSelectRunner(sql, data, options)

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val flow = executor.executeQuery(statement) { dialect, row ->
            transform(R2dbcRowWrapper(dialect, row))
        }
        return collect(flow)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
