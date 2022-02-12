package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.query.Row
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcTemplateSelectRunner<T, R>(
    context: TemplateSelectContext,
    transform: (Row) -> T,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcRunner<R> {

    private val flowBuilder = R2dbcTemplateSelectFlowBuilder(context, transform)

    override fun check(config: DatabaseConfig) {
        flowBuilder.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val flow = flowBuilder.build(config)
        return collect(flow)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return flowBuilder.dryRun(config)
    }
}
