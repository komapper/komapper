package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.option.EntityBatchDeleteOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityBatchDeleteQuery<ENTITY : Any> : Query<Unit> {
    fun option(configurator: QueryOptionConfigurator<EntityBatchDeleteOption>): EntityBatchDeleteQuery<ENTITY>
}

internal data class EntityBatchDeleteQueryImpl<ENTITY : Any>(
    private val context: EntityDeleteContext<ENTITY>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchDeleteOption = EntityBatchDeleteOption()
) :
    EntityBatchDeleteQuery<ENTITY> {

    private val support: EntityDeleteQuerySupport<ENTITY> = EntityDeleteQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityBatchDeleteOption>): EntityBatchDeleteQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig) {
        if (entities.isEmpty()) return
        val statements = entities.map { buildStatement(config, it) }
        val (counts) = delete(config, statements)
        postDelete(counts)
    }

    private fun delete(config: DatabaseConfig, statements: List<Statement>): Pair<IntArray, LongArray> {
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(counts: IntArray) {
        for ((i, count) in counts.withIndex()) {
            support.postDelete(count, i)
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, entities.first())
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
