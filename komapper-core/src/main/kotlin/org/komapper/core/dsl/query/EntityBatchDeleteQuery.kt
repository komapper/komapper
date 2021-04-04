package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.scope.EntityBatchDeleteOptionDeclaration
import org.komapper.core.dsl.scope.EntityBatchDeleteOptionScope

interface EntityBatchDeleteQuery<ENTITY> : Query<Unit> {
    fun option(declaration: EntityBatchDeleteOptionDeclaration): EntityBatchDeleteQuery<ENTITY>
}

internal data class EntityBatchDeleteQueryImpl<ENTITY>(
    private val context: EntityDeleteContext<ENTITY>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchDeleteOption = QueryOptionImpl()
) :
    EntityBatchDeleteQuery<ENTITY> {

    private val support: EntityDeleteQuerySupport<ENTITY> = EntityDeleteQuerySupport(context, option)

    override fun option(declaration: EntityBatchDeleteOptionDeclaration): EntityBatchDeleteQueryImpl<ENTITY> {
        val scope = EntityBatchDeleteOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig) {
        val statements = entities.map { buildStatement(config.dialect, it) }
        val (counts) = delete(config, statements)
        postDelete(counts)
    }

    private fun delete(config: DatabaseConfig, statements: List<Statement>): Pair<IntArray, LongArray> {
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(counts: IntArray) {
        for ((i, count) in counts.withIndex()) {
            support.postDelete(count, i)
        }
    }

    override fun dryRun(dialect: Dialect): Statement {
        return buildStatement(dialect, entities.first())
    }

    private fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        return support.buildStatement(dialect, entity)
    }
}
