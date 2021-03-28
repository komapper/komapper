package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlUpdateStatementBuilder
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.core.jdbc.JdbcExecutor

interface SqlUpdateQuery : Query<Int> {
    fun set(declaration: SetDeclaration): SqlUpdateQuery
    fun where(declaration: WhereDeclaration): SqlUpdateQuery

    override fun peek(dialect: Dialect, block: (Statement) -> Unit): SqlUpdateQuery {
        super.peek(dialect, block)
        return this
    }
}

internal data class SqlUpdateQueryImpl<ENTITY>(
    private val context: SqlUpdateContext<ENTITY>
) : SqlUpdateQuery {

    override fun set(declaration: SetDeclaration): SqlUpdateQueryImpl<ENTITY> {
        val scope = SetScope()
        declaration(scope)
        val newContext = context.addSet(scope.context.toList())
        return SqlUpdateQueryImpl(newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlUpdateQueryImpl<ENTITY> {
        val scope = WhereScope()
        declaration(scope)
        val newContext = context.addWhere(scope.criteria.toList())
        return SqlUpdateQueryImpl(newContext)
    }

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config)
        return executor.executeUpdate(statement) { _, count ->
            count
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = SqlUpdateStatementBuilder(dialect, context)
        return builder.build()
    }
}
