package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationDeleteReturningRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcRelationDeleteReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
    private val transform: (R2dbcDataOperator, Row) -> T,
) : R2dbcRunner<List<T>> {

    private val runner: RelationDeleteReturningRunner<ENTITY, ID, META> = RelationDeleteReturningRunner(context)

    override suspend fun run(config: R2dbcDatabaseConfig): List<T> {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, context.options)
        val flow = executor.executeQuery(statement, transform)
        return flow.toList()
    }

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
