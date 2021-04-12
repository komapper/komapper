package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.option.EntityDeleteOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityDeleteQuery<ENTITY : Any> : Query<Unit> {
    fun option(configurator: QueryOptionConfigurator<EntityDeleteOption>): EntityDeleteQuery<ENTITY>
}

internal data class EntityDeleteQueryImpl<ENTITY : Any>(
    private val context: EntityDeleteContext<ENTITY>,
    private val entity: ENTITY,
    private val option: EntityDeleteOption = EntityDeleteOption()
) :
    EntityDeleteQuery<ENTITY> {

    private val support: EntityDeleteQuerySupport<ENTITY> = EntityDeleteQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityDeleteOption>): EntityDeleteQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val (count) = delete(config, statement)
        postDelete(count)
    }

    private fun delete(config: DatabaseConfig, statement: Statement): Pair<Int, LongArray> {
        return support.delete(config) { it.executeUpdate(statement) }
    }

    private fun postDelete(count: Int) {
        support.postDelete(count)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        return support.buildStatement(config, entity)
    }
}
