package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlSetOperationStatementBuilder
import org.komapper.core.dsl.context.SqlSetOperationComponent
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.element.SortIndex
import org.komapper.core.dsl.scope.SqlSetOperationOptionDeclaration
import org.komapper.core.dsl.scope.SqlSetOperationOptionScope
import org.komapper.core.jdbc.JdbcExecutor
import java.sql.ResultSet

interface SqlSetOperationQuery<T> : SqlSetOperandQuery<T> {
    fun orderBy(vararg indexes: Number): SqlSetOperationQuery<T>
    fun option(declaration: SqlSetOperationOptionDeclaration): SqlSetOperationQuery<T>
}

internal data class SetOperationQueryImpl<T>(
    private val context: SqlSetOperationContext<T>,
    private val option: SqlSetOperationOption = QueryOptionImpl(allowEmptyWhereClause = true),
    private val provider: (Dialect, ResultSet) -> T
) : SqlSetOperationQuery<T> {

    override val setOperationComponent = context.component

    override fun except(other: SqlSetOperandQuery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    override fun intersect(other: SqlSetOperandQuery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    override fun union(other: SqlSetOperandQuery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    override fun unionAll(other: SqlSetOperandQuery<T>): SetOperationQueryImpl<T> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(kind: SqlSetOperationKind, other: SqlSetOperandQuery<T>): SetOperationQueryImpl<T> {
        val component = SqlSetOperationComponent.Composite(kind, setOperationComponent, other.setOperationComponent)
        val newContext = context.copy(component = component)
        return copy(context = newContext)
    }

    override fun orderBy(vararg indexes: Number): SetOperationQueryImpl<T> {
        val sortIndexes = indexes.map {
            if (it is SortIndex) it else SortIndex.Asc(it)
        }
        val newContext = context.copy(orderBy = context.orderBy + sortIndexes)
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

    override fun dryRun(dialect: Dialect): Statement {
        val terminal = createTerminal { it.toList() }
        return terminal.dryRun(dialect)
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
                checkWhereClauses(context.component)
            }
            val statement = buildStatement(config.dialect)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(statement, provider, transformer)
        }

        private fun checkWhereClauses(component: SqlSetOperationComponent<*>) {
            when (component) {
                is SqlSetOperationComponent.Leaf -> {
                    if (component.context.where.isEmpty()) {
                        error("Empty where clause is not allowed.")
                    }
                }
                is SqlSetOperationComponent.Composite -> {
                    checkWhereClauses(component.left)
                    checkWhereClauses(component.right)
                }
            }
        }

        override fun dryRun(dialect: Dialect): Statement {
            return buildStatement(dialect)
        }

        private fun buildStatement(dialect: Dialect): Statement {
            val builder = SqlSetOperationStatementBuilder(dialect, context)
            return builder.build()
        }
    }
}
