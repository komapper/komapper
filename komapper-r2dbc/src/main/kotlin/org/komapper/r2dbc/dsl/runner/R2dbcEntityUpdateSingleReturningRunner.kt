package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpdateSingleReturningRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpdateSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : R2dbcRunner<ENTITY?> {

    private val runner: EntityUpdateSingleReturningRunner<ENTITY, ID, META> =
        EntityUpdateSingleReturningRunner(context, entity)

    private val support: R2dbcEntityUpdateReturningRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpdateReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY? {
        val newEntity = preUpdate(config, entity)
        val entity = update(config, newEntity)
        return postUpdate(entity)
    }

    private fun preUpdate(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return runner.preUpdate(config, entity)
    }

    private suspend fun update(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY? {
        val statement = runner.buildStatement(config, entity)
        return support.update(config) { executor ->
            val transform = R2dbcRowTransformers.singleEntity(context.target)
            val flow = executor.executeQuery(statement, transform)
            flow.firstOrNull()
        }
    }

    private fun postUpdate(entity: ENTITY?): ENTITY? {
        return runner.postUpdate(entity)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
