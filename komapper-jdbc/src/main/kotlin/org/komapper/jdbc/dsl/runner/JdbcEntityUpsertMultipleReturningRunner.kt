package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertMultipleReturningRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertMultipleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : JdbcRunner<List<ENTITY>> {

    private val runner: EntityUpsertMultipleReturningRunner<ENTITY, ID, META> =
        EntityUpsertMultipleReturningRunner(context, entities)

    private val support: JdbcEntityUpsertReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entities: List<ENTITY>): List<ENTITY> {
        val statement = runner.buildStatement(config, entities)
        return support.upsert(config) { executor ->
            val transform = JdbcResultSetTransformers.singleEntity(context.target)
            executor.execute(statement, transform) { it.toList() }
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
