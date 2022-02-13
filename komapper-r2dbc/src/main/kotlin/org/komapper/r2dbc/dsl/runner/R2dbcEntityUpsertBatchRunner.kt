package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertBatchRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpsertBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : R2dbcRunner<List<Int>> {

    private val runner: EntityUpsertBatchRunner<ENTITY, ID, META> =
        EntityUpsertBatchRunner(context, entities)

    private val support: R2dbcEntityUpsertRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): List<Int> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = entities.map { preUpsert(config, it) }
        val batchResults = upsert(config, newEntities)
        return batchResults.map { it.first }
    }

    private suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private suspend fun upsert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): List<Pair<Int, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.upsert(config, false) { it.executeBatch(statements) }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
