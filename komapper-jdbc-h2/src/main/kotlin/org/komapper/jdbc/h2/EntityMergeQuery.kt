package org.komapper.jdbc.h2

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.query.Query

interface EntityMergeQuery<ENTITY : Any> : Query<Pair<Int, LongArray>> {
    fun on(vararg keys: PropertyMetamodel<ENTITY, *>): EntityMergeQuery<ENTITY>
    fun option(configurator: QueryOptionConfigurator<EntityMergeOption>): EntityMergeQuery<ENTITY>
}

internal data class EntityMergeQueryImpl<ENTITY : Any>(
    val context: EntityMergeContext<ENTITY>,
    val entity: ENTITY,
    val option: EntityMergeOption = EntityMergeOption()
) : EntityMergeQuery<ENTITY> {

    private val support: EntityMergeQuerySupport<ENTITY> = EntityMergeQuerySupport(context, option)

    override fun on(vararg keys: PropertyMetamodel<ENTITY, *>): EntityMergeQueryImpl<ENTITY> {
        val newContext = context.copy(on = keys.toList())
        return copy(context = newContext)
    }

    override fun option(configurator: QueryOptionConfigurator<EntityMergeOption>): EntityMergeQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): Pair<Int, LongArray> {
        val newEntity = preMerge(config)
        val statement = buildStatement(config, newEntity)
        return merge(config, statement).also { (count, _) ->
            postMerge(count)
        }
    }

    private fun preMerge(config: DatabaseConfig): ENTITY {
        return support.preMerge(config, entity)
    }

    private fun merge(config: DatabaseConfig, statement: Statement): Pair<Int, LongArray> {
        return support.merge(config) { it.executeUpdate(statement) }
    }

    private fun postMerge(count: Int) {
        support.postMerge(count)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config, entity).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
