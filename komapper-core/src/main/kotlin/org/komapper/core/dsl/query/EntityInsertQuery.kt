package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityInsertQuery<ENTITY : Any> : Query<ENTITY> {
    fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityInsertQuery<ENTITY>
}

internal data class EntityInsertQueryImpl<ENTITY : Any>(
    private val context: EntityInsertContext<ENTITY>,
    private val entity: ENTITY,
    private val option: EntityInsertOption = EntityInsertOption()
) :
    EntityInsertQuery<ENTITY> {

    private val support: EntityInsertQuerySupport<ENTITY> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityInsertQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): ENTITY {
        val newEntity = preInsert(config)
        val statement = buildStatement(config, newEntity)
        val (_, generatedKeys) = insert(config, statement)
        return postInsert(newEntity, generatedKeys)
    }

    private fun preInsert(config: DatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private fun insert(config: DatabaseConfig, statement: Statement): Pair<Int, LongArray> {
        return support.insert(config) { it.executeUpdate(statement) }
    }

    private fun postInsert(entity: ENTITY, generatedKeys: LongArray): ENTITY {
        val key = generatedKeys.firstOrNull()
        return if (key == null) {
            entity
        } else {
            support.postInsert(entity, key)
        }
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config, entity).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
