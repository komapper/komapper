package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpdateBatchRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpdateBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) :
    R2dbcRunner<List<ENTITY>> {

    private val runner: EntityUpdateBatchRunner<ENTITY, ID, META> =
        EntityUpdateBatchRunner(context, entities)

    private val support: R2dbcEntityUpdateRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpdateRunnerSupport(context)

    override suspend fun run(config: R2dbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        if (!config.dialect.supportsBatchRunOfParameterizedStatement()) {
            throw UnsupportedOperationException("The dialect(driver=${config.dialect.driver}) does not support a batch run.")
        }
        val newEntities = preUpdate(config)
        val batchResults = update(config, newEntities)
        return postUpdate(newEntities, batchResults)
    }

    private fun preUpdate(config: R2dbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preUpdate(config, it) }
    }

    private suspend fun update(config: R2dbcDatabaseConfig, entities: List<ENTITY>): List<Pair<Int, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.update(config) { it.executeBatch(statements) }
    }

    private fun postUpdate(entities: List<ENTITY>, batchResults: List<Pair<Int, Long?>>): List<ENTITY> {
        val iterator = batchResults.map { it.first }.iterator()
        return entities.mapIndexed { index, entity ->
            val count = if (iterator.hasNext()) {
                iterator.next()
            } else {
                error("Count value is not found. index=$index")
            }
            support.postUpdate(entity, count, index)
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
