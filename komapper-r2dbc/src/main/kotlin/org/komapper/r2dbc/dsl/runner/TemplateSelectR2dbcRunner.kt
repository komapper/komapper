package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Row
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class TemplateSelectR2dbcRunner<T, R>(
    sql: String,
    data: Any,
    private val transform: (Row) -> T,
    private val options: TemplateSelectOptions,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcRunner<R> {

    private val flowBuilder = TemplateSelectFlowBuilder(sql, data, transform, options)

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val flow = flowBuilder.build(config)
        return collect(flow)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return flowBuilder.dryRun(config)
    }
}
