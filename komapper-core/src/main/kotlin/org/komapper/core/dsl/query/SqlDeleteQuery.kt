package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.SqlExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlDeleteStatementBuilder
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlDeleteOption
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope

interface SqlDeleteQuery : Query<Int> {
    fun where(declaration: WhereDeclaration): SqlDeleteQuery
    fun option(configurator: (SqlDeleteOption) -> SqlDeleteOption): SqlDeleteQuery
}

internal data class SqlDeleteQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlDeleteContext<ENTITY, ID, META>,
    private val option: SqlDeleteOption = SqlDeleteOption()
) : SqlDeleteQuery {

    override fun where(declaration: WhereDeclaration): SqlDeleteQueryImpl<ENTITY, ID, META> {
        val scope = WhereScope().apply(declaration)
        val newContext = context.copy(where = context.where + scope)
        return copy(context = newContext)
    }

    override fun option(configurator: (SqlDeleteOption) -> SqlDeleteOption): SqlDeleteQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator(option))
    }

    override fun run(holder: DatabaseConfigHolder): Int {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val config = holder.config
        val statement = buildStatement(config, context)
        val executor = SqlExecutor(config, option)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        return buildStatement(config, context).sql
    }

    private fun buildStatement(config: DatabaseConfig, c: SqlDeleteContext<ENTITY, ID, META>): Statement {
        val builder = SqlDeleteStatementBuilder(config.dialect, c)
        return builder.build()
    }
}
