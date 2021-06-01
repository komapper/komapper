package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlUpdateStatementBuilder
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlUpdateOption
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface SqlUpdateQuery<ENTITY : Any> : Query<Int> {
    fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQuery<ENTITY>
    fun where(declaration: WhereDeclaration): SqlUpdateQuery<ENTITY>
    fun option(configure: (SqlUpdateOption) -> SqlUpdateOption): SqlUpdateQuery<ENTITY>
}

internal data class SqlUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlUpdateContext<ENTITY, ID, META>,
    private val option: SqlUpdateOption = SqlUpdateOption.default
) : SqlUpdateQuery<ENTITY> {

    override fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQueryImpl<ENTITY, ID, META> {
        val scope = SetScope<ENTITY>().apply(declaration)
        val newContext = context.copy(set = context.set + scope)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlUpdateQueryImpl<ENTITY, ID, META> {
        val scope = WhereScope().apply(declaration)
        val newContext = context.copy(where = context.where + scope)
        return copy(context = newContext)
    }

    override fun option(configure: (SqlUpdateOption) -> SqlUpdateOption): SqlUpdateQueryImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = SqlUpdateStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
