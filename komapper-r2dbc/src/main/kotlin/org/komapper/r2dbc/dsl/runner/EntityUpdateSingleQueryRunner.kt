package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityUpdateOption
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityUpdateSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    option: EntityUpdateOption,
    private val entity: ENTITY
) : R2dbcQueryRunner<ENTITY> {

    private val support: EntityUpdateQueryRunnerSupport<ENTITY, ID, META> = EntityUpdateQueryRunnerSupport(context, option)

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY {
        val newEntity = preUpdate(config, entity)
        val (count) = update(config, newEntity)
        return postUpdate(newEntity, count)
    }

    private fun preUpdate(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpdate(config, entity)
    }

    private suspend fun update(config: R2dbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = buildStatement(config, entity)
        return support.update(config) { it.executeUpdate(statement) }
    }

    private fun postUpdate(entity: ENTITY, count: Int): ENTITY {
        return support.postUpdate(entity, count)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
