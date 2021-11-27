package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityDeleteBatchRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityDeleteBatchJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) :
    JdbcRunner<Unit> {

    private val runner: EntityDeleteBatchRunner<ENTITY, ID, META> =
        EntityDeleteBatchRunner(context, entities)

    private val support: EntityDeleteJdbcRunnerSupport<ENTITY, ID, META> =
        EntityDeleteJdbcRunnerSupport(context)

    override fun run(config: JdbcDatabaseConfig) {
        if (entities.isEmpty()) return
        val (counts) = delete(config)
        postDelete(counts)
    }

    private fun delete(config: JdbcDatabaseConfig): Pair<IntArray, LongArray> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(counts: IntArray) {
        for ((i, count) in counts.withIndex()) {
            support.postDelete(count, i)
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
