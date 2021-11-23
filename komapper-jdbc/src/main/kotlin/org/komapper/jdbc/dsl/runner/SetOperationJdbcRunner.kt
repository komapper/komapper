package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.runner.SetOperationRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

internal class SetOperationJdbcRunner<T : Any?, R>(
    context: SetOperationContext,
    private val options: SelectOptions,
    private val transform: (JdbcDialect, ResultSet) -> T,
    private val collect: suspend (Flow<T>) -> R
) : JdbcRunner<R> {

    private val runner: SetOperationRunner = SetOperationRunner(context, options)

    override fun run(config: JdbcDatabaseConfig): R {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        return executor.executeQuery(statement, transform, collect)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
