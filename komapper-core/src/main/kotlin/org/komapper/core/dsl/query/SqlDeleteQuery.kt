package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlDeleteStatementBuilder
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.SqlDeleteOption
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope

interface SqlDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun option(configurator: QueryOptionConfigurator<SqlDeleteOption>): SqlDeleteQuery
}

internal data class SqlDeleteQueryImpl<ENTITY : Any>(
    private val context: SqlDeleteContext<ENTITY>,
    private val option: SqlDeleteOption = SqlDeleteOption()
) : SqlDeleteQuery {

    override fun where(declaration: WhereDeclaration): SqlDeleteQueryImpl<ENTITY> {
        val scope = WhereScope()
        declaration(scope)
        val newContext = context.addWhere(scope.toList())
        return copy(context = newContext)
    }

    override fun option(configurator: QueryOptionConfigurator<SqlDeleteOption>): SqlDeleteQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): Int {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config, context)
        val executor = JdbcExecutor(config, option.asJdbcOption())
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, context)
    }

    private fun buildStatement(config: DatabaseConfig, c: SqlDeleteContext<ENTITY>): Statement {
        val builder = SqlDeleteStatementBuilder(config.dialect, c)
        return builder.build()
    }
}
