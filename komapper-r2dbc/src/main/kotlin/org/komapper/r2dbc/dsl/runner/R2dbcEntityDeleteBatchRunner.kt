package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityDeleteBatchRunner
import org.komapper.core.dsl.runner.customizeBatchCount
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

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig) {
        if (entities.isEmpty()) return
        val batchResults = delete(config)
        postDelete(batchResults.map { it.first })
    }

    private suspend fun delete(config: R2dbcDatabaseConfig): List<Pair<Int, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.delete(config) { it.executeBatch(statements, ::customizeBatchCount) }
    }

    private fun postDelete(counts: List<Int>) {
        runner.postDelete(counts)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
