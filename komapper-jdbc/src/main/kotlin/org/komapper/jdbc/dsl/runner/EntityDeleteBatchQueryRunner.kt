package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityDeleteBatchOption
import org.komapper.jdbc.DatabaseConfig

internal data class EntityDeleteBatchQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityDeleteBatchOption
) :
    JdbcQueryRunner<Unit> {

    private val support: EntityDeleteQuerySupport<ENTITY, ID, META> = EntityDeleteQuerySupport(context, option)

    override fun run(config: DatabaseConfig) {
        if (entities.isEmpty()) return
        val (counts) = delete(config)
        postDelete(counts)
    }

    private fun delete(config: DatabaseConfig): Pair<IntArray, LongArray> {
        val statements = entities.map { buildStatement(config, it) }
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(counts: IntArray) {
        for ((i, count) in counts.withIndex()) {
            support.postDelete(count, i)
        }
    }

    override fun dryRun(config: DatabaseConfig): String {
        if (entities.isEmpty()) return ""
        return buildStatement(config, entities.first()).asSql()
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
