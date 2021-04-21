package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityUpdateOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityUpdateQuery<ENTITY : Any> : Query<Unit> {
    fun option(configurator: QueryOptionConfigurator<EntityUpdateOption>): EntityUpdateQuery<ENTITY>
    fun include(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Unit>
    fun exclude(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Unit>
}

internal data class EntityUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val option: EntityUpdateOption = EntityUpdateOption()
) :
    EntityUpdateQuery<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY, ID, META> = EntityUpdateQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityUpdateOption>): EntityUpdateQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator.apply(option))
    }

    override fun include(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Unit> {
        val newContext = support.include(propertyMetamodels.toList())
        return copy(context = newContext)
    }

    override fun exclude(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Unit> {
        val newContext = support.exclude(propertyMetamodels.toList())
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig) {
        val newEntity = preUpdate(config, entity)
        val (count) = update(config, newEntity)
        return postUpdate(count)
    }

    private fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpdate(config, entity)
    }

    private fun update(config: DatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = buildStatement(config, entity)
        return support.update(config) { it.executeUpdate(statement) }
    }

    private fun postUpdate(count: Int) {
        return support.postUpdate(count)
    }

    override fun dryRun(config: DatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
