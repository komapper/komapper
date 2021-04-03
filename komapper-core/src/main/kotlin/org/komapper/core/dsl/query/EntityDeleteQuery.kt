package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.scope.EntityDeleteOptionDeclaration
import org.komapper.core.dsl.scope.EntityDeleteOptionScope

interface EntityDeleteQuery<ENTITY> : Query<Unit> {
    fun option(declaration: EntityDeleteOptionDeclaration): EntityDeleteQuery<ENTITY>
}

internal data class EntityDeleteQueryImpl<ENTITY>(
    private val context: EntityDeleteContext<ENTITY>,
    private val entity: ENTITY,
    private val option: EntityDeleteOption = QueryOptionImpl()
) :
    EntityDeleteQuery<ENTITY> {

    private val support: EntityDeleteQuerySupport<ENTITY> = EntityDeleteQuerySupport(context, option)

    override fun option(declaration: EntityDeleteOptionDeclaration): EntityDeleteQueryImpl<ENTITY> {
        val scope = EntityDeleteOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun execute(config: DatabaseConfig) {
        val statement = buildStatement(config.dialect)
        val (count) = delete(config, statement)
        postDelete(count)
    }

    private fun delete(config: DatabaseConfig, statement: Statement): Pair<Int, LongArray> {
        return support.delete(config) { it.executeUpdate(statement) }
    }

    private fun postDelete(count: Int) {
        support.postDelete(count)
    }

    override fun statement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        return support.buildStatement(dialect, entity)
    }
}
