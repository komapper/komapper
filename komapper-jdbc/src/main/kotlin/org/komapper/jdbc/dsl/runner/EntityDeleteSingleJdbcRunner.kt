package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.core.dsl.runner.EntityDeleteSingleRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityDeleteSingleJdbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    options: EntityDeleteOptions,
    entity: ENTITY
) : JdbcRunner<Unit> {

    private val runner: EntityDeleteSingleRunner<ENTITY, ID, META> =
        EntityDeleteSingleRunner(context, options, entity)

    private val support: EntityDeleteJdbcRunnerSupport<ENTITY, ID, META> =
        EntityDeleteJdbcRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig) {
        val (count) = delete(config)
        postDelete(count)
    }

    private fun delete(config: JdbcDatabaseConfig): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config)
        return support.delete(config) { it.executeUpdate(statement) }
    }

    private fun postDelete(count: Int) {
        support.postDelete(count)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
