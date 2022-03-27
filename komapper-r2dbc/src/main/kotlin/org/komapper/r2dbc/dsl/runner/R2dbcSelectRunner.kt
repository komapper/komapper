package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcSelectRunner<T, R>(
    context: SelectContext<*, *, *>,
    transform: (R2dbcDataOperator, Row) -> T,
    private val collect: suspend (Flow<T>) -> R
) :
    R2dbcRunner<R> {

    private val flowBuilder: R2dbcSelectFlowBuilder<T> = R2dbcSelectFlowBuilder(context, transform)

    override fun check(config: DatabaseConfig) {
        flowBuilder.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val flow = flowBuilder.build(config)
        // TODO: This is a workaround to avoid timeouts in SQL Server. However, it is unclear if this workaround is effective.
        return if (config.dialect.driver == "sqlserver") {
            // consume all data
            val newFlow = flow.toList().asFlow()
            collect(newFlow)
        } else {
            collect(flow)
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return flowBuilder.dryRun(config)
    }
}
