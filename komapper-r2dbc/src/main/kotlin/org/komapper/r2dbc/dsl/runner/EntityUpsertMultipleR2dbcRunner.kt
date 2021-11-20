package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.runner.EntityUpsertMultipleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityUpsertMultipleR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    private val entities: List<ENTITY>
) : R2dbcRunner<Int> {

    private val runner: EntityUpsertMultipleRunner<ENTITY, ID, META> =
        EntityUpsertMultipleRunner(context, options, entities)

    private val support: EntityUpsertR2dbcRunnerSupport<ENTITY, ID, META> =
        EntityUpsertR2dbcRunnerSupport(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        if (entities.isEmpty()) return 0
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private suspend fun upsert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): Int {
        val statement = runner.buildStatement(config, entities)
        val (count) = support.upsert(config) { it.executeUpdate(statement) }
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
