package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.runner.SetOperationRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal class SetOperationFlowBuilder<T>(
    private val context: SetOperationContext,
    private val transform: (R2dbcDialect, Row) -> T
) : FlowBuilder<T> {

    private val runner: SetOperationRunner = SetOperationRunner(context)

    override fun build(config: R2dbcDatabaseConfig): Flow<T> {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, context.options)
        return executor.executeQuery(statement, transform)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
