package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpdateBatchRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpdateBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) :
    JdbcRunner<List<ENTITY>> {

    private val runner: EntityUpdateBatchRunner<ENTITY, ID, META> =
        EntityUpdateBatchRunner(context, entities)

    private val support: JdbcEntityUpdateRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpdateRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preUpdate(config)
        val batchResults = update(config, newEntities)
        return postUpdate(newEntities, batchResults.map { it.first })
    }

    private fun preUpdate(config: JdbcDatabaseConfig): List<ENTITY> {
        return entities.map { runner.preUpdate(config, it) }
    }

    private fun update(config: JdbcDatabaseConfig, entities: List<ENTITY>): List<Pair<Long, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.update(config) { it.executeBatch(statements) }
    }

    private fun postUpdate(entities: List<ENTITY>, counts: List<Long>): List<ENTITY> {
        return runner.postUpdate(entities, counts)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
