package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityDeleteSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    options: EntityDeleteOptions,
    private val entity: ENTITY
) : JdbcQueryRunner<Unit> {

    private val support: EntityDeleteQueryRunnerSupport<ENTITY, ID, META> =
        EntityDeleteQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig) {
        val (count) = delete(config)
        postDelete(count)
    }

    private fun delete(config: JdbcDatabaseConfig): Pair<Int, LongArray> {
        val statement = buildStatement(config)
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
