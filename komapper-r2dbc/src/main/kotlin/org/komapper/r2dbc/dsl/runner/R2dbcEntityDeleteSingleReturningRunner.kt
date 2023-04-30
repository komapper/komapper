package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.singleOrNull
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityDeleteSingleReturningRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityDeleteSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    entity: ENTITY,
    private val transform: (R2dbcDataOperator, Row) -> T,
) : R2dbcRunner<T?> {

    private val runner: EntityDeleteSingleReturningRunner<ENTITY, ID, META> =
        EntityDeleteSingleReturningRunner(context, entity)

    private val support: R2dbcEntityDeleteReturningRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityDeleteReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): T? {
        val result = delete(config)
        val count = if (result == null) 0L else 1L
        postDelete(count)
        return result
    }

    private suspend fun delete(config: R2dbcDatabaseConfig): T? {
        val statement = runner.buildStatement(config)
        return support.delete(config) { executor ->
            val flow = executor.executeQuery(statement, transform)
            flow.singleOrNull()
        }
    }

    private fun postDelete(count: Long) {
        runner.postDelete(count)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
