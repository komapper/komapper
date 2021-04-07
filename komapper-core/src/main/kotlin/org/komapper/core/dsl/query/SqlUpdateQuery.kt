package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlUpdateStatementBuilder
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.SqlUpdateOptionDeclaration
import org.komapper.core.dsl.scope.SqlUpdateOptionScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.core.jdbc.JdbcExecutor

interface SqlUpdateQuery : Query<Int> {
    fun set(declaration: SetDeclaration): SqlUpdateQuery
    fun where(declaration: WhereDeclaration): SqlUpdateQuery
    fun option(declaration: SqlUpdateOptionDeclaration): SqlUpdateQuery
}

internal data class SqlUpdateQueryImpl<ENTITY : Any>(
    private val context: SqlUpdateContext<ENTITY>,
    private val option: SqlUpdateOption = QueryOptionImpl()
) : SqlUpdateQuery {

    override fun set(declaration: SetDeclaration): SqlUpdateQueryImpl<ENTITY> {
        val scope = SetScope()
        declaration(scope)
        val newContext = context.addSet(scope.toList())
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlUpdateQueryImpl<ENTITY> {
        val scope = WhereScope()
        declaration(scope)
        val newContext = context.addWhere(scope.toList())
        return copy(context = newContext)
    }

    override fun option(declaration: SqlUpdateOptionDeclaration): SqlUpdateQueryImpl<ENTITY> {
        val scope = SqlUpdateOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): Int {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config, option.asJdbcOption())
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = SqlUpdateStatementBuilder(dialect, context)
        return builder.build()
    }
}
