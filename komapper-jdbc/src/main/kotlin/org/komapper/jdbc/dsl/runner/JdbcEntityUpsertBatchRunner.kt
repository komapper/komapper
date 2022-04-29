package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertBatchRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : JdbcRunner<List<Long>> {

    private val runner: EntityUpsertBatchRunner<ENTITY, ID, META> =
        EntityUpsertBatchRunner(context, entities)

    private val support: JdbcEntityUpsertRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<Long> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = entities.map { preUpsert(config, it) }
        val batchResults = upsert(config, newEntities)
        return batchResults.map { it.first }
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entities: List<ENTITY>): List<Pair<Long, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.upsert(config, false) { it.executeBatch(statements) }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
