package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlDeleteStatementBuilder
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.scope.SqlDeleteOptionDeclaration
import org.komapper.core.dsl.scope.SqlDeleteOptionScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.core.jdbc.JdbcExecutor

interface SqlDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun option(declaration: SqlDeleteOptionDeclaration): SqlDeleteQuery
    override fun peek(dialect: Dialect, block: (Statement) -> Unit): SqlDeleteQuery {
        super.peek(dialect, block)
        return this
    }
}

internal data class SqlDeleteQueryImpl<ENTITY>(
    private val context: SqlDeleteContext<ENTITY>,
    private val option: SqlDeleteOption = QueryOptionImpl()
) : SqlDeleteQuery {

    override fun where(declaration: WhereDeclaration): SqlDeleteQueryImpl<ENTITY> {
        val scope = WhereScope()
        declaration(scope)
        val newContext = context.addWhere(scope.criteria.toList())
        return copy(context = newContext)
    }

    override fun option(declaration: SqlDeleteOptionDeclaration): SqlDeleteQueryImpl<ENTITY> {
        val scope = SqlDeleteOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): Int {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config.dialect, context)
        val executor = JdbcExecutor(config, option.asJdbcOption())
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect, context)
    }

    private fun buildStatement(dialect: Dialect, c: SqlDeleteContext<ENTITY>): Statement {
        val builder = SqlDeleteStatementBuilder(dialect, c)
        return builder.build()
    }
}
