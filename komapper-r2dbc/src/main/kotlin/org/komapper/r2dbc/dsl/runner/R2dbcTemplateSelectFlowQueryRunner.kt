package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.TemplateSelectQueryRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcTemplateSelectFlowQueryRunner<T>(
    sql: String,
    params: Any,
    private val transform: (Row) -> T,
    private val options: TemplateSelectOptions
) : R2dbcFlowQueryRunner<T> {

    private val runner = TemplateSelectQueryRunner(sql, params, options)

    override fun run(config: R2dbcDatabaseConfig): Flow<T> {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        return executor.executeQuery(statement) { dialect, row ->
            transform(R2dbcRowWrapper(dialect, row))
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
