package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityBatchUpdateOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityBatchUpdateQuery<ENTITY : Any> : Query<List<ENTITY>> {
    fun option(configurator: QueryOptionConfigurator<EntityBatchUpdateOption>): EntityBatchUpdateQuery<ENTITY>
}

internal data class EntityBatchUpdateQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: EntityUpdateContext<ENTITY, META>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchUpdateOption = EntityBatchUpdateOption()
) :
    EntityBatchUpdateQuery<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY, META> = EntityUpdateQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityBatchUpdateOption>): EntityBatchUpdateQueryImpl<ENTITY, META> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preUpdate(config)
        val statements = newEntities.map { buildStatement(config, it) }
        val (counts) = update(config, statements)
        return postUpdate(newEntities, counts)
    }

    private fun preUpdate(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preUpdate(config, it) }
    }

    private fun update(config: DatabaseConfig, statements: List<Statement>): Pair<IntArray, LongArray> {
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

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config, entities.first()).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
