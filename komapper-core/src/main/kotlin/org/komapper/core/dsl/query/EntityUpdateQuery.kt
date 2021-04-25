package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityUpdateOption

interface EntityUpdateQuery<ENTITY : Any> : Query<ENTITY> {
    fun option(configurator: (EntityUpdateOption) -> EntityUpdateOption): EntityUpdateQuery<ENTITY>
    fun include(vararg properties: PropertyMetamodel<ENTITY, *>): Query<ENTITY>
    fun exclude(vararg properties: PropertyMetamodel<ENTITY, *>): Query<ENTITY>
}

internal data class EntityUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val option: EntityUpdateOption = EntityUpdateOption()
) :
    EntityUpdateQuery<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY, ID, META> = EntityUpdateQuerySupport(context, option)

    override fun option(configurator: (EntityUpdateOption) -> EntityUpdateOption): EntityUpdateQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator(option))
    }

    override fun include(vararg properties: PropertyMetamodel<ENTITY, *>): Query<ENTITY> {
        val newContext = support.include(properties.toList())
        return copy(context = newContext)
    }

    override fun exclude(vararg properties: PropertyMetamodel<ENTITY, *>): Query<ENTITY> {
        val newContext = support.exclude(properties.toList())
        return copy(context = newContext)
    }

    override fun run(holder: DatabaseConfigHolder): ENTITY {
        val config = holder.config
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

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
