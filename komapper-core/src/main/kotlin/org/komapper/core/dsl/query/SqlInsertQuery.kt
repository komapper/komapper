package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlInsertStatementBuilder
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.scope.SqlInsertOptionDeclaration
import org.komapper.core.dsl.scope.SqlInsertOptionScope
import org.komapper.core.dsl.scope.ValuesDeclaration
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.Assignment

interface SqlInsertQuery : Query<Pair<Int, LongArray>> {
    fun values(declaration: ValuesDeclaration): SqlInsertQuery
    fun option(declaration: SqlInsertOptionDeclaration): SqlInsertQuery
}

internal data class SqlInsertQueryImpl<ENTITY>(
    private val context: SqlInsertContext<ENTITY>,
    private val option: SqlInsertOption = QueryOptionImpl()
) : SqlInsertQuery {

    override fun values(declaration: ValuesDeclaration): SqlInsertQueryImpl<ENTITY> {
        val scope = ValuesScope()
        declaration(scope)
        val newContext = context.addValues(scope.toList())
        return copy(context = newContext)
    }

    override fun option(declaration: SqlInsertOptionDeclaration): SqlInsertQueryImpl<ENTITY> {
        val scope = SqlInsertOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): Pair<Int, LongArray> {
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config, option.asJdbcOption()) { con, sql ->
            val assignment = context.entityMetamodel.idAssignment()
            if (assignment is Assignment.Identity<*, *>) {
                con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            } else {
                con.prepareStatement(sql)
            }
        }
        return executor.executeUpdate(statement)
    }

    override fun dryRun(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = SqlInsertStatementBuilder(dialect, context)
        return builder.build()
    }
}
