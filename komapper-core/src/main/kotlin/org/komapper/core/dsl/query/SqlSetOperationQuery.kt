package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.Dialect
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.AliasManagerImpl
import org.komapper.core.dsl.builder.SqlSetOperationStatementBuilder
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.scope.SqlSetOperationOptionDeclaration
import org.komapper.core.dsl.scope.SqlSetOperationOptionScope
import java.sql.ResultSet

interface SqlSetOperationQuery<T> : Subquery<T> {
    fun orderBy(vararg aliases: CharSequence): SqlSetOperationQuery<T>
    fun orderBy(vararg expressions: PropertyExpression<*>): SqlSetOperationQuery<T>
    fun option(declaration: SqlSetOperationOptionDeclaration): SqlSetOperationQuery<T>
}

internal data class SetOperationQueryImpl<T>(
    private val context: SqlSetOperationContext<T>,
    private val option: SqlSetOperationOption = QueryOptionImpl(allowEmptyWhereClause = true),
    private val provider: (Dialect, ResultSet) -> T
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

    override fun orderBy(vararg expressions: PropertyExpression<*>): SetOperationQueryImpl<T> {
        val items = expressions.map {
            if (it is SortItem) it else SortItem.Property.Asc(it)
        }
        return orderBy(items)
    }

    private fun orderBy(items: List<SortItem>): SetOperationQueryImpl<T> {
        val newContext = context.copy(orderBy = context.orderBy + items)
        return copy(context = newContext)
    }

    override fun option(declaration: SqlSetOperationOptionDeclaration): SetOperationQueryImpl<T> {
        val scope = SqlSetOperationOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): List<T> {
        val terminal = createTerminal { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        val terminal = createTerminal { it.toList() }
        return terminal.dryRun(config)
    }

    override fun first(): Query<T> {
        return createTerminal { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return createTerminal { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<T>) -> R): Query<R> {
        return createTerminal(transformer)
    }

    private fun <R> createTerminal(transformer: (Sequence<T>) -> R): Query<R> {
        return Terminal(context, option, provider, transformer)
    }

    private class Terminal<T, R>(
        private val context: SqlSetOperationContext<T>,
        private val option: SqlSetOperationOption,
        private val provider: (Dialect, ResultSet) -> T,
        val transformer: (Sequence<T>) -> R
    ) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause) {
                checkWhereClauses(context.left)
                checkWhereClauses(context.right)
            }
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(statement, provider, transformer)
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

        override fun dryRun(config: DatabaseConfig): Statement {
            return buildStatement(config)
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val aliasManager = AliasManagerImpl(context)
            val builder = SqlSetOperationStatementBuilder(config.dialect, context, aliasManager)
            return builder.build()
        }
    }
}
