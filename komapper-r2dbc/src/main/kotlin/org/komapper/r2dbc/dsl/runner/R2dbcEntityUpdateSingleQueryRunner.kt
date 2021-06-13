package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityUpdateOptions
import org.komapper.core.dsl.runner.EntityUpdateSingleQueryRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpdateSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    options: EntityUpdateOptions,
    private val entity: ENTITY
) : R2dbcQueryRunner<ENTITY> {

    private val runner: EntityUpdateSingleQueryRunner<ENTITY, ID, META> =
        EntityUpdateSingleQueryRunner(context, options, entity)

    private val support: R2dbcEntityUpdateQueryRunnerSupport<ENTITY, ID, META> = R2dbcEntityUpdateQueryRunnerSupport(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY {
        val newEntity = preUpdate(config, entity)
        val (count) = update(config, newEntity)
        return postUpdate(newEntity, count)
    }

    private fun preUpdate(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpdate(config, entity)
    }

    private suspend fun update(config: R2dbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config, entity)
        return support.update(config) { it.executeUpdate(statement) }
    }

    private fun postUpdate(entity: ENTITY, count: Int): ENTITY {
        return support.postUpdate(entity, count)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
