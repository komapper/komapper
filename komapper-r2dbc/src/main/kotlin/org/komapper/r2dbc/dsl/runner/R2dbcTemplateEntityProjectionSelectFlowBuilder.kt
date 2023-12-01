package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.runner.TemplateSelectRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcTemplateEntityProjectionSelectFlowBuilder<T>(
    private val context: TemplateSelectContext,
    private val transform: (R2dbcDataOperator, Row) -> T,
) : R2dbcFlowBuilder<T> {

    private val runner = TemplateSelectRunner(context)

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
