package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.scope.EntityBatchDeleteOptionDeclaration
import org.komapper.core.dsl.scope.EntityBatchDeleteOptionScope

interface EntityBatchDeleteQuery<ENTITY : Any> : Query<Unit> {
    fun option(declaration: EntityBatchDeleteOptionDeclaration): EntityBatchDeleteQuery<ENTITY>
}

internal data class EntityBatchDeleteQueryImpl<ENTITY : Any>(
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
        val statements = entities.map { buildStatement(config, it) }
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

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, entities.first())
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
