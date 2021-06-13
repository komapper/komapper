package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.runner.EntityUpsertMultipleQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertMultipleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    val entities: List<ENTITY>
) : JdbcQueryRunner<Int> {

    private val runner: EntityUpsertMultipleQueryRunner<ENTITY, ID, META> =
        EntityUpsertMultipleQueryRunner(context, options, entities)

    private val support: JdbcEntityUpsertQueryRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpsertQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig): Int {
        if (entities.isEmpty()) return 0
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entities: List<ENTITY>): Int {
        val statement = runner.buildStatement(config, entities)
        val (count) = support.upsert(config) { it.executeUpdate(statement) }
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
