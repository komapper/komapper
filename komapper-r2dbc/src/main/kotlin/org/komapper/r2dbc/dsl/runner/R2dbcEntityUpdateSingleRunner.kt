package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpdateSingleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpdateSingleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY
) : R2dbcRunner<ENTITY> {

    private val runner: EntityUpdateSingleRunner<ENTITY, ID, META> =
        EntityUpdateSingleRunner(context, entity)

    private val support: R2dbcEntityUpdateRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpdateRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY {
        val newEntity = preUpdate(config, entity)
        val (count) = update(config, newEntity)
        return postUpdate(newEntity, count)
    }

    private fun preUpdate(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return runner.preUpdate(config, entity)
    }

    private suspend fun update(config: R2dbcDatabaseConfig, entity: ENTITY): Pair<Int, List<Long>> {
        val statement = runner.buildStatement(config, entity)
        return support.update(config) { it.executeUpdate(statement) }
    }

    private fun postUpdate(entity: ENTITY, count: Int): ENTITY {
        return runner.postUpdate(entity, count)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
