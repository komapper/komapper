package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityDeleteBatchRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityDeleteBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) :
    R2dbcRunner<Unit> {

    private val runner: EntityDeleteBatchRunner<ENTITY, ID, META> =
        EntityDeleteBatchRunner(context, entities)

    private val support: R2dbcEntityDeleteRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityDeleteRunnerSupport(context)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        if (entities.isEmpty()) return
        if (!config.dialect.supportsBatchRunOfParameterizedStatement()) {
            throw UnsupportedOperationException("The dialect(driver=${config.dialect.driver}) does not support a batch run.")
        }
        val results = delete(config)
        postDelete(results)
    }

    private suspend fun delete(config: R2dbcDatabaseConfig): List<Pair<Int, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(results: List<Pair<Int, Long?>>) {
        for ((i, result) in results.withIndex()) {
            support.postDelete(result.first, i)
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
