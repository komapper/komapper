package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpdateSingleReturningRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpdateSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val transform: (R2dbcDataOperator, Row) -> T,
) : R2dbcRunner<T?> {

    private val runner: EntityUpdateSingleReturningRunner<ENTITY, ID, META> =
        EntityUpdateSingleReturningRunner(context, entity)

    private val support: R2dbcEntityUpdateReturningRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpdateReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): T? {
        val newEntity = preUpdate(config, entity)
        return update(config, newEntity).also {
            postUpdate(it)
        }
    }

    private fun preUpdate(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return runner.preUpdate(config, entity)
    }

    private suspend fun update(config: R2dbcDatabaseConfig, entity: ENTITY): T? {
        val statement = runner.buildStatement(config, entity)
        return support.update(config) { executor ->
            val flow = executor.executeQuery(statement, transform)
            flow.firstOrNull()
        }
    }

    private fun postUpdate(result: Any?) {
        runner.postUpdate(result)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
