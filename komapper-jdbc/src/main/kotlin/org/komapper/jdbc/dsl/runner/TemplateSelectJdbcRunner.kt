package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.runner.TemplateSelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class TemplateSelectJdbcRunner<T, R>(
    private val context: TemplateSelectContext,
    private val transform: (Row) -> T,
    private val collect: suspend (Flow<T>) -> R,
) : JdbcRunner<R> {

    private val runner = TemplateSelectRunner(context)

    override fun run(config: JdbcDatabaseConfig): R {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, context.options)
        return executor.executeQuery(
            statement,
            { dialect, rs ->
                val row = JdbcResultSetWrapper(dialect, rs)
                transform(row)
            },
            collect
        )
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
