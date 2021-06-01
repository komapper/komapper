package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlDeleteStatementBuilder
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlDeleteOption
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface SqlDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun option(configure: (SqlDeleteOption) -> SqlDeleteOption): SqlDeleteQuery
}

internal data class SqlDeleteQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlDeleteContext<ENTITY, ID, META>,
    private val option: SqlDeleteOption = SqlDeleteOption.default
) : SqlDeleteQuery {

    override fun where(declaration: WhereDeclaration): SqlDeleteQueryImpl<ENTITY, ID, META> {
        val scope = WhereScope().apply(declaration)
        val newContext = context.copy(where = context.where + scope)
        return copy(context = newContext)
    }

    override fun option(configure: (SqlDeleteOption) -> SqlDeleteOption): SqlDeleteQueryImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config, context)
        val executor = R2dbcExecutor(config, option)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config, context).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig, c: SqlDeleteContext<ENTITY, ID, META>): Statement {
        val builder = SqlDeleteStatementBuilder(config.dialect, c)
        return builder.build()
    }
}
