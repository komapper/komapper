package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlDeleteStatementBuilder
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.core.jdbc.JdbcExecutor

interface SqlDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): SqlDeleteQuery

    override fun peek(dialect: Dialect, block: (Statement) -> Unit): SqlDeleteQuery {
        super.peek(dialect, block)
        return this
    }
}

internal data class SqlDeleteQueryImpl<ENTITY>(
    private val context: SqlDeleteContext<ENTITY>
) : SqlDeleteQuery {

    override fun where(declaration: WhereDeclaration): SqlDeleteQueryImpl<ENTITY> {
        val scope = WhereScope()
        declaration(scope)
        val newContext = context.addWhere(scope.criteria.toList())
        return SqlDeleteQueryImpl(newContext)
    }

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config.dialect, context)
        val executor = JdbcExecutor(config)
        return executor.executeUpdate(statement) { _, count ->
            count
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect, context)
    }

    private fun buildStatement(dialect: Dialect, c: SqlDeleteContext<ENTITY>): Statement {
        val builder = SqlDeleteStatementBuilder(dialect, c)
        return builder.build()
    }
}
