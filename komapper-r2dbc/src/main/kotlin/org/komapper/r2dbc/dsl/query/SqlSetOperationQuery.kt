package org.komapper.r2dbc.dsl.query

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.DefaultAliasManager
import org.komapper.core.dsl.builder.SqlSetOperationStatementBuilder
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

interface SqlSetOperationQuery<T> : Subquery<T> {
    fun orderBy(vararg aliases: CharSequence): SqlSetOperationQuery<T>
    fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSetOperationQuery<T>
    fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SqlSetOperationQuery<T>
}

internal data class SetOperationQueryImpl<T>(
    private val context: SqlSetOperationContext<T>,
    private val option: SqlSetOperationOption = SqlSetOperationOption.default,
    private val provide: (R2dbcDialect, Row) -> T
) : SqlSetOperationQuery<T> {

    override val subqueryContext = SubqueryContext.SqlSetOperation(context)

    override fun except(other: Subquery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    override fun intersect(other: Subquery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    override fun union(other: Subquery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    override fun unionAll(other: Subquery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(kind: SqlSetOperationKind, other: Subquery<T>): SetOperationQueryImpl<T> {
        val newContext =
            context.copy(
                kind = kind,
                left = SubqueryContext.SqlSetOperation(context),
                right = other.subqueryContext
            )
        return copy(context = newContext)
    }

    override fun orderBy(vararg aliases: CharSequence): SetOperationQueryImpl<T> {
        val items = aliases.map {
            if (it is SortItem) it else SortItem.Alias.Asc(it.toString())
        }
        return orderBy(items)
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SetOperationQueryImpl<T> {
        val items = expressions.map {
            if (it is SortItem) it else SortItem.Property.Asc(it)
        }
        return orderBy(items)
    }

    private fun orderBy(items: List<SortItem>): SetOperationQueryImpl<T> {
        val newContext = context.copy(orderBy = context.orderBy + items)
        return copy(context = newContext)
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SetOperationQueryImpl<T> {
        return copy(option = configurator(option))
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Flow<T> {
        if (!option.allowEmptyWhereClause) {
            checkWhereClauses(context.left)
            checkWhereClauses(context.right)
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        return executor.executeQuery(statement) { row, _ ->
            provide(config.dialect, row)
        }
    }

    private fun checkWhereClauses(subqueryContext: SubqueryContext<*>) {
        when (subqueryContext) {
            is SubqueryContext.EntitySelect -> {
                if (subqueryContext.context.where.isEmpty()) {
                    error("Empty where clause is not allowed.")
                }
            }
            is SubqueryContext.SqlSelect -> {
                if (subqueryContext.context.where.isEmpty()) {
                    error("Empty where clause is not allowed.")
                }
            }
            is SubqueryContext.SqlSetOperation -> {
                checkWhereClauses(subqueryContext.context.left)
                checkWhereClauses(subqueryContext.context.right)
            }
        }
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val aliasManager = DefaultAliasManager(context)
        val builder = SqlSetOperationStatementBuilder(config.dialect, context, aliasManager)
        return builder.build()
    }
}
