package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlInsertStatementBuilder
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlInsertOption
import org.komapper.core.dsl.scope.ValuesDeclaration
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface SqlInsertQuery<ENTITY : Any> : Query<Pair<Int, Long?>> {
    fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY>
    fun <T : Any> select(block: () -> Subquery<T>): SqlInsertQuery<ENTITY>
    fun option(configure: (SqlInsertOption) -> SqlInsertOption): SqlInsertQuery<ENTITY>
}

internal data class SqlInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val option: SqlInsertOption = SqlInsertOption.default
) : SqlInsertQuery<ENTITY> {

    override fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQueryImpl<ENTITY, ID, META> {
        val scope = ValuesScope<ENTITY>().apply(declaration)
        val values = when (val values = context.values) {
            is Values.Pairs -> Values.Pairs(values.pairs + scope)
            is Values.Subquery -> Values.Pairs(scope.toList())
        }
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun <T : Any> select(block: () -> Subquery<T>): SqlInsertQuery<ENTITY> {
        val subquery = block()
        val values = Values.Subquery(subquery.subqueryContext)
        val newContext = context.copy(values = values)
        return copy(context = newContext)
    }

    override fun option(configure: (SqlInsertOption) -> SqlInsertOption): SqlInsertQueryImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Pair<Int, Long?> {
        val statement = buildStatement(config)
        val generatedColumn = when (context.target.idAssignment()) {
            // TODO
            is Assignment.AutoIncrement<ENTITY, *, *> -> context.target.idProperties().first().columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, option, generatedColumn)
        val (count, keys) = executor.executeUpdate(statement)
        return count to keys.firstOrNull()
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = SqlInsertStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
