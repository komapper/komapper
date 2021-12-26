package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.ColumnsImpl
import org.komapper.jdbc.JdbcDialect
import java.sql.ResultSet

internal object JdbcResultSetTransformers {

    fun <T : Any> singleEntity(metamodel: EntityMetamodel<T, *, *>): (JdbcDialect, ResultSet) -> T = { dialect, rs ->
        val mapper = JdbcEntityMapper(dialect, rs)
        val entity = mapper.execute(metamodel, true)
        checkNotNull(entity)
    }

    fun <A : Any> singleColumn(expression: ColumnExpression<A, *>): (JdbcDialect, ResultSet) -> A? {
        return singleColumn(expression) { it }
    }

    fun <A : Any> singleNotNullColumn(expression: ColumnExpression<A, *>): (JdbcDialect, ResultSet) -> A {
        return singleColumn(expression) {
            checkNotNull(it) { "The value of the selected column is null." }
        }
    }

    private fun <A : Any, R> singleColumn(expression: ColumnExpression<A, *>, transform: (A?) -> R): (JdbcDialect, ResultSet) -> R =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val value = mapper.execute(expression)
            transform(value)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (JdbcDialect, ResultSet) -> Pair<A?, B?> =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            first to second
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (JdbcDialect, ResultSet) -> Triple<A?, B?, C?> =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            val third = mapper.execute(expressions.third)
            Triple(first, second, third)
        }

    fun multipleColumns(expressions: List<ColumnExpression<*, *>>): (JdbcDialect, ResultSet) -> Columns =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val map = expressions.associateWith { mapper.execute(it) }
            ColumnsImpl(map)
        }
}
