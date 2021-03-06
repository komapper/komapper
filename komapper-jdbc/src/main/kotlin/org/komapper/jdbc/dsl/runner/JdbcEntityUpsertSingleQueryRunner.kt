package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.runner.EntityUpsertSingleQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    private val entity: ENTITY,
) : JdbcQueryRunner<Int> {

    private val runner: EntityUpsertSingleQueryRunner<ENTITY, ID, META> =
        EntityUpsertSingleQueryRunner(context, options, entity)

    private val support: JdbcEntityUpsertQueryRunnerSupport<ENTITY, ID, META> = JdbcEntityUpsertQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig): Int {
        val newEntity = preUpsert(config, entity)
        val (count) = upsert(config, newEntity)
        return count
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config) { it.executeUpdate(statement) }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
