package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertMultipleReturningRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpsertMultipleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : R2dbcRunner<List<ENTITY>> {

    private val runner: EntityUpsertMultipleReturningRunner<ENTITY, ID, META> =
        EntityUpsertMultipleReturningRunner(context, entities)

    private val support: R2dbcEntityUpsertReturningRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private suspend fun upsert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): List<ENTITY> {
        val statement = runner.buildStatement(config, entities)
        return support.upsert(config, false) { executor ->
            val transform = R2dbcRowTransformers.singleEntity(context.target)
            val flow = executor.executeQuery(statement, transform)
            flow.toList()
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
