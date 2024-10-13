package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationUpdateReturningRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcRelationUpdateReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    private val transform: (R2dbcDataOperator, Row) -> T,
) : R2dbcRunner<List<T>> {
    private val runner: RelationUpdateReturningRunner<ENTITY, ID, META> = RelationUpdateReturningRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): List<T> {
        val clock = config.clockProvider.now()
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        val result = runner.buildStatement(config, updatedAtAssignment)
        val statement = result.getOrNull()
        return if (statement != null) {
            val executor = R2dbcExecutor(config, context.options)
            val flow = executor.executeQuery(statement, transform)
            flow.toList()
        } else {
            emptyList()
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
