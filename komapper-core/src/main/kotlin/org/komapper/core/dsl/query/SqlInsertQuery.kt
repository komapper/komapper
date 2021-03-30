package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlInsertStatementBuilder
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.scope.SqlInsertOptionsDeclaration
import org.komapper.core.dsl.scope.SqlInsertOptionsScope
import org.komapper.core.dsl.scope.ValuesDeclaration
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.Assignment

interface SqlInsertQuery : Query<Pair<Int, Long?>> {
    fun values(declaration: ValuesDeclaration): SqlInsertQuery
    fun options(declaration: SqlInsertOptionsDeclaration): SqlInsertQuery

    override fun peek(dialect: Dialect, block: (Statement) -> Unit): SqlInsertQuery {
        super.peek(dialect, block)
        return this
    }
}

internal data class SqlInsertQueryImpl<ENTITY>(
    private val context: SqlInsertContext<ENTITY>
) : SqlInsertQuery {

    override fun values(declaration: ValuesDeclaration): SqlInsertQueryImpl<ENTITY> {
        val scope = ValuesScope()
        declaration(scope)
        val newContext = context.addValues(scope.context.toList())
        return copy(context = newContext)
    }

    override fun options(declaration: SqlInsertOptionsDeclaration): SqlInsertQueryImpl<ENTITY> {
        val scope = SqlInsertOptionsScope(context.options)
        declaration(scope)
        val newContext = context.copy(options = scope.options)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): Pair<Int, Long?> {
        val executor = JdbcExecutor(config, context.options) { con, sql ->
            val assignment = context.entityMetamodel.idAssignment()
            if (assignment is Assignment.Identity<*, *>) {
                con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            } else {
                con.prepareStatement(sql)
            }
        }

        val statement = buildStatement(config.dialect)
        return executor.executeUpdate(statement) { ps, count ->
            val assignment = context.entityMetamodel.idAssignment()
            val id = if (assignment is Assignment.Identity<ENTITY, *>) {
                ps.generatedKeys.use { rs ->
                    if (rs.next()) rs.getLong(1) else error("No result: Statement.generatedKeys")
                }
            } else {
                null
            }
            count to id
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = SqlInsertStatementBuilder(dialect, context)
        return builder.build()
    }
}
