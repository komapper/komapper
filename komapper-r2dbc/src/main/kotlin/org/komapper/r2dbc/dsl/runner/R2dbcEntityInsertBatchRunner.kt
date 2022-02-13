package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertBatchRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityInsertBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : R2dbcRunner<List<ENTITY>> {

    private val runner: EntityInsertBatchRunner<ENTITY, ID, META> =
        EntityInsertBatchRunner(context, entities)

    private val support: R2dbcEntityInsertRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityInsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val batchResults = insert(config, newEntities)
        return postInsert(newEntities, batchResults.map { it.second })
    }

    private suspend fun preInsert(config: R2dbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): List<Pair<Int, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.insert(config, true) { it.executeBatch(statements) }
    }

    private fun postInsert(entities: List<ENTITY>, generatedKeys: List<Long?>): List<ENTITY> {
        return runner.postInsert(entities, generatedKeys)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
