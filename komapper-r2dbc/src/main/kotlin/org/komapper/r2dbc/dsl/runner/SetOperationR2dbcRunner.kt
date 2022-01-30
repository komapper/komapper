package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect

internal class SetOperationR2dbcRunner<T, R>(
    context: SetOperationContext,
    transform: (R2dbcDialect, Row) -> T,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcRunner<R> {

    private val flowBuilder: SetOperationFlowBuilder<T> = SetOperationFlowBuilder(context, transform)

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val flow = flowBuilder.build(config)
        return collect(flow)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return flowBuilder.dryRun(config)
    }
}
