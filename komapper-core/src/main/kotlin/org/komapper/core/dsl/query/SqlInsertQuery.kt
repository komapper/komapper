package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlInsertStatementBuilder
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.SqlInsertOption
import org.komapper.core.dsl.scope.ValuesDeclaration
import org.komapper.core.dsl.scope.ValuesScope

interface SqlInsertQuery<ENTITY : Any> : Query<Pair<Int, LongArray>> {
    fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQuery<ENTITY>
    fun <T : Any> select(block: () -> Subquery<T>): SqlInsertQuery<ENTITY>
    fun option(configurator: QueryOptionConfigurator<SqlInsertOption>): SqlInsertQuery<ENTITY>
}

internal data class SqlInsertQueryImpl<ENTITY : Any>(
    private val context: SqlInsertContext<ENTITY>,
    private val option: SqlInsertOption = SqlInsertOption()
) : SqlInsertQuery<ENTITY> {

    override fun values(declaration: ValuesDeclaration<ENTITY>): SqlInsertQueryImpl<ENTITY> {
        val scope = ValuesScope<ENTITY>()
        declaration(scope)
        val values = when (val values = context.values) {
            is Values.Pairs -> Values.Pairs(values.pairs + scope.toList())
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

    override fun option(configurator: QueryOptionConfigurator<SqlInsertOption>): SqlInsertQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): Pair<Int, LongArray> {
        val statement = buildStatement(config)
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

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlInsertStatementBuilder(config.dialect, context)
        return builder.build()
    }
}

interface SqlInsertQueryBuilder<T : Any> {
    fun values(declaration: ValuesDeclaration<T>): SqlInsertQuery<T>
    fun select(block: () -> Subquery<T>): SqlInsertQuery<T>
}

internal class SqlInsertQueryBuilderImpl<T : Any>(val query: SqlInsertQuery<T>) : SqlInsertQueryBuilder<T> {
    override fun values(declaration: ValuesDeclaration<T>): SqlInsertQuery<T> {
        return query.values(declaration)
    }

    override fun select(block: () -> Subquery<T>): SqlInsertQuery<T> {
        return query.select(block)
    }
}
