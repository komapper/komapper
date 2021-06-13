package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteBatchOptions
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityDeleteBatchQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    options: EntityDeleteBatchOptions,
    private val entities: List<ENTITY>
) :
    JdbcQueryRunner<Unit> {

    private val support: EntityDeleteQueryRunnerSupport<ENTITY, ID, META> =
        EntityDeleteQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig) {
        if (entities.isEmpty()) return
        val (counts) = delete(config)
        postDelete(counts)
    }

    private fun delete(config: JdbcDatabaseConfig): Pair<IntArray, LongArray> {
        val statements = entities.map { buildStatement(config, it) }
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(counts: IntArray) {
        for ((i, count) in counts.withIndex()) {
            support.postDelete(count, i)
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        if (entities.isEmpty()) return Statement.EMPTY
        return buildStatement(config, entities.first())
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
