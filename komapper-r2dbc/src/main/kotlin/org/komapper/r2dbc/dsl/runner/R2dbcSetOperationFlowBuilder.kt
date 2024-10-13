package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.runner.SetOperationRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcSetOperationFlowBuilder<T>(
    private val context: SetOperationContext,
    private val transform: (R2dbcDataOperator, Row) -> T,
) : R2dbcFlowBuilder<T> {
    private val runner: SetOperationRunner = SetOperationRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun build(config: R2dbcDatabaseConfig): Flow<T> {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, context.options)
        return executor.executeQuery(statement, transform)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
