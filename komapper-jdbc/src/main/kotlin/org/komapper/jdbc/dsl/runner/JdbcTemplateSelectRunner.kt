package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.TemplateSelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcTemplateSelectRunner<T, R>(
    private val context: TemplateSelectContext,
    private val transform: (Row) -> T,
    private val collect: suspend (Flow<T>) -> R,
) : JdbcRunner<R> {

    private val runner = TemplateSelectRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): R {
        val statement = runner.buildStatement(config)
        val executor = config.dialect.createExecutor(config, context.options)
        return executor.executeQuery(
            statement,
            { dataOperator, rs ->
                val row = JdbcResultSetWrapper(dataOperator, rs)
                transform(row)
            },
            collect,
        )
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
