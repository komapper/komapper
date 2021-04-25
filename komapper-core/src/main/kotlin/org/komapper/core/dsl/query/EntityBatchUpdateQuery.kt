package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityBatchUpdateOption

interface EntityBatchUpdateQuery<ENTITY : Any> : Query<List<ENTITY>> {
    fun option(configurator: (EntityBatchUpdateOption) -> EntityBatchUpdateOption): EntityBatchUpdateQuery<ENTITY>
    fun include(vararg properties: PropertyMetamodel<ENTITY, *>): Query<List<ENTITY>>
    fun exclude(vararg properties: PropertyMetamodel<ENTITY, *>): Query<List<ENTITY>>
}

internal data class EntityBatchUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchUpdateOption = EntityBatchUpdateOption()
) :
    EntityBatchUpdateQuery<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY, ID, META> = EntityUpdateQuerySupport(context, option)

    override fun option(configurator: (EntityBatchUpdateOption) -> EntityBatchUpdateOption): EntityBatchUpdateQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator(option))
    }

    override fun include(vararg properties: PropertyMetamodel<ENTITY, *>): Query<List<ENTITY>> {
        val newContext = support.include(properties.toList())
        return copy(context = newContext)
    }

    override fun exclude(vararg properties: PropertyMetamodel<ENTITY, *>): Query<List<ENTITY>> {
        val newContext = support.exclude(properties.toList())
        return copy(context = newContext)
    }

    override fun run(holder: DatabaseConfigHolder): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val config = holder.config
        val newEntities = preUpdate(config)
        val (counts) = update(config, newEntities)
        return postUpdate(newEntities, counts)
    }

    private fun preUpdate(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preUpdate(config, it) }
    }

    private fun update(config: DatabaseConfig, entities: List<ENTITY>): Pair<IntArray, LongArray> {
        val statements = entities.map { buildStatement(config, it) }
        return support.update(config) { it.executeBatch(statements) }
    }

    private fun postUpdate(entities: List<ENTITY>, counts: IntArray): List<ENTITY> {
        val iterator = counts.iterator()
        return entities.mapIndexed { index, entity ->
            val count = if (iterator.hasNext()) {
                iterator.nextInt()
            } else {
                error("Count value is not found. index=$index")
            }
            support.postUpdate(entity, count, index)
        }
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        if (entities.isEmpty()) return ""
        val config = holder.config
        return buildStatement(config, entities.first()).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
