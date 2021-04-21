package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityBatchUpdateOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityBatchUpdateQuery<ENTITY : Any> : Query<Unit> {
    fun option(configurator: QueryOptionConfigurator<EntityBatchUpdateOption>): EntityBatchUpdateQuery<ENTITY>
    fun include(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Unit>
    fun exclude(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Unit>
}

internal data class EntityBatchUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchUpdateOption = EntityBatchUpdateOption()
) :
    EntityBatchUpdateQuery<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY, ID, META> = EntityUpdateQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityBatchUpdateOption>): EntityBatchUpdateQueryImpl<ENTITY, ID, META> {
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
        if (entities.isEmpty()) return
        val newEntities = preUpdate(config)
        val (counts) = update(config, newEntities)
        postUpdate(newEntities, counts)
    }

    private fun preUpdate(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preUpdate(config, it) }
    }

    private fun update(config: DatabaseConfig, entities: List<ENTITY>): Pair<IntArray, LongArray> {
        val statements = entities.map { buildStatement(config, it) }
        return support.update(config) { it.executeBatch(statements) }
    }

    private fun postUpdate(entities: List<ENTITY>, counts: IntArray) {
        val iterator = counts.iterator()
        entities.forEachIndexed { index, entity ->
            val count = if (iterator.hasNext()) {
                iterator.nextInt()
            } else {
                error("Count value is not found. index=$index")
            }
            support.postUpdate(count, index)
        }
    }

    override fun dryRun(config: DatabaseConfig): String {
        if (entities.isEmpty()) return ""
        return buildStatement(config, entities.first()).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
