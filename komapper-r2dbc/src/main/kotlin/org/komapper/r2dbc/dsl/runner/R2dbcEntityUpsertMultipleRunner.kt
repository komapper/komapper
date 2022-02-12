package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertMultipleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpsertMultipleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : R2dbcRunner<Int> {

    private val runner: EntityUpsertMultipleRunner<ENTITY, ID, META> =
        EntityUpsertMultipleRunner(context, entities)

    private val support: R2dbcEntityUpsertRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

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
        val (count) = support.upsert(config, false) { it.executeUpdate(statement) }
        return count
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
