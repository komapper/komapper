package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityUpdateOption
import org.komapper.jdbc.DatabaseConfig

internal data class EntityUpdateSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val option: EntityUpdateOption,
    private val entity: ENTITY
) : JdbcQueryRunner<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY, ID, META> = EntityUpdateQuerySupport(context, option)

    override fun run(config: DatabaseConfig): ENTITY {
        val newEntity = preUpdate(config, entity)
        val (count) = update(config, newEntity)
        return postUpdate(newEntity, count)
    }

    private fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpdate(config, entity)
    }

    private fun update(config: DatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = buildStatement(config, entity)
        return support.update(config) { it.executeUpdate(statement) }
    }

    private fun postUpdate(entity: ENTITY, count: Int): ENTITY {
        return support.postUpdate(entity, count)
    }

    override fun dryRun(config: DatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.toSql()
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
