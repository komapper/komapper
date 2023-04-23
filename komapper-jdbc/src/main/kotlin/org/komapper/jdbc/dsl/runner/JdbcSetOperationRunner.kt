package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.runner.SetOperationRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import java.sql.ResultSet

internal class JdbcSetOperationRunner<T : Any?, R>(
    private val context: SetOperationContext,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
    private val collect: suspend (Flow<T>) -> R,
) : JdbcRunner<R> {

    private val runner: SetOperationRunner = SetOperationRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): R {
        val statement = runner.buildStatement(config)
        val executor = config.dialect.createExecutor(config, context.options)
        return executor.executeQuery(statement, transform, collect)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
