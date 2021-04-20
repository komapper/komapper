package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityUpdateOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityUpdateQuery<ENTITY : Any> : Query<ENTITY> {
    fun option(configurator: QueryOptionConfigurator<EntityUpdateOption>): EntityUpdateQuery<ENTITY>
    fun include(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<ENTITY?>
    fun exclude(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<ENTITY?>
}

internal data class EntityUpdateQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: EntityUpdateContext<ENTITY, META>,
    private val entity: ENTITY,
    private val option: EntityUpdateOption = EntityUpdateOption()
) :
    EntityUpdateQuery<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY, META> = EntityUpdateQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityUpdateOption>): EntityUpdateQueryImpl<ENTITY, META> {
        return copy(option = configurator.apply(option))
    }

    override fun include(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<ENTITY?> {
        val newContext = support.include(propertyMetamodels.toList())
        val query = copy(context = newContext)
        return wrap(query)
    }

    override fun exclude(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<ENTITY?> {
        val newContext = support.exclude(propertyMetamodels.toList())
        val query = copy(context = newContext)
        return wrap(query)
    }

    private fun wrap(original: EntityUpdateQueryImpl<ENTITY, META>): Query<ENTITY?> {
        return object : Query<ENTITY?> {
            override fun run(config: DatabaseConfig): ENTITY? {
                if (original.context.getTargetProperties().isEmpty()) return null
                return original.run(config)
            }

            override fun dryRun(config: DatabaseConfig): String {
                if (original.context.getTargetProperties().isEmpty()) return ""
                return original.dryRun(config)
            }
        }
    }

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
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
