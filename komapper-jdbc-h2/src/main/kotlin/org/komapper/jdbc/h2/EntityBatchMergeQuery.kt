package org.komapper.jdbc.h2

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.query.Query

interface EntityBatchMergeQuery<ENTITY : Any> : Query<Pair<IntArray, LongArray>> {
    fun on(vararg keys: PropertyMetamodel<ENTITY, *>): EntityBatchMergeQuery<ENTITY>
    fun option(configurator: QueryOptionConfigurator<EntityBatchMergeOption>): EntityBatchMergeQuery<ENTITY>
}

internal data class EntityBatchMergeQueryImpl<ENTITY : Any>(
    val context: EntityMergeContext<ENTITY>,
    val entities: List<ENTITY>,
    val option: EntityBatchMergeOption = EntityBatchMergeOption()
) : EntityBatchMergeQuery<ENTITY> {

    private val support: EntityMergeQuerySupport<ENTITY> = EntityMergeQuerySupport(context, option)

    override fun on(vararg keys: PropertyMetamodel<ENTITY, *>): EntityBatchMergeQueryImpl<ENTITY> {
        val newContext = context.copy(on = keys.toList())
        return copy(context = newContext)
    }

    override fun option(configurator: QueryOptionConfigurator<EntityBatchMergeOption>): EntityBatchMergeQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): Pair<IntArray, LongArray> {
        // TODO
        if (entities.isEmpty()) IntArray(0) to LongArray(0)
        val newEntities = preMerge(config)
        val statements = newEntities.map { buildStatement(config, it) }
        return merge(config, statements).also { (counts, _) ->
            postMerge(counts)
        }
    }

    private fun preMerge(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preMerge(config, it) }
    }

    private fun merge(config: DatabaseConfig, statements: List<Statement>): Pair<IntArray, LongArray> {
        return support.merge(config) { it.executeBatch(statements) }
    }

    private fun postMerge(counts: IntArray) {
        for (count in counts) {
            support.postMerge(count)
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, entities.first())
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
