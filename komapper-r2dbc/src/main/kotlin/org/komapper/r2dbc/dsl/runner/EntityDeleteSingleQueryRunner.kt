package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityDeleteSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    options: EntityDeleteOptions,
    private val entity: ENTITY
) : R2dbcQueryRunner<Unit> {

    private val support: EntityDeleteQueryRunnerSupport<ENTITY, ID, META> = EntityDeleteQueryRunnerSupport(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val (count) = delete(config)
        postDelete(count)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toSql()
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
