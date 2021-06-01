package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityDeleteOption
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal data class EntityDeleteSingleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val option: EntityDeleteOption
) : Query<Unit> {

    private val support: EntityDeleteQuerySupport<ENTITY, ID, META> = EntityDeleteQuerySupport(context, option)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val (count) = delete(config)
        postDelete(count)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private suspend fun delete(config: R2dbcDatabaseConfig): Pair<Int, LongArray> {
        val statement = buildStatement(config)
        return support.delete(config) { it.executeUpdate(statement) }
    }

    private fun postDelete(count: Int) {
        support.postDelete(count)
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        return support.buildStatement(config, entity)
    }
}
