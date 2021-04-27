package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityBatchUpdateOption

internal data class EntityUpdateBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchUpdateOption
) :
    Query<List<ENTITY>> {

    private val support: EntityUpdateQuerySupport<ENTITY, ID, META> = EntityUpdateQuerySupport(context, option)

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
