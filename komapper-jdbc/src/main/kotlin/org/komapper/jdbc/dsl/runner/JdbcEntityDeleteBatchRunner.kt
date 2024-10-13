package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityDeleteBatchRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityDeleteBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) :
    JdbcRunner<Unit> {
    private val runner: EntityDeleteBatchRunner<ENTITY, ID, META> =
        EntityDeleteBatchRunner(context, entities)

    private val support: JdbcEntityDeleteRunnerSupport<ENTITY, ID, META> =
        JdbcEntityDeleteRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig) {
        if (entities.isEmpty()) return
        val batchResults = delete(config)
        postDelete(batchResults.map { it.first })
    }

    private fun delete(config: JdbcDatabaseConfig): List<Pair<Long, Long?>> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(counts: List<Long>) {
        runner.postDelete(counts)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
