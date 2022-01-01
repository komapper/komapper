package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.RecordImpl
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

    private fun <A : Any, R> singleColumn(
        expression: ColumnExpression<A, *>,
        transform: (A?) -> R
    ): (JdbcDialect, ResultSet) -> R =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val value = mapper.execute(expression)
            transform(value)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (JdbcDialect, ResultSet) -> Pair<A?, B?> {
        return pairColumns(expressions) { it }
    }

    fun <A : Any, B : Any> pairNotNullColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (JdbcDialect, ResultSet) -> Pair<A, B> {
        return pairColumns(expressions) {
            val first = checkNotNull(it.first) { "The value of the first column is null." }
            val second = checkNotNull(it.second) { "The value of the second column is null." }
            first to second
        }
    }

    private fun <A : Any, B : Any, AR, BR> pairColumns(
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        transform: (Pair<A?, B?>) -> Pair<AR, BR>
    ): (JdbcDialect, ResultSet) -> Pair<AR, BR> =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            transform(first to second)
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (JdbcDialect, ResultSet) -> Triple<A?, B?, C?> {
        return tripleColumns(expressions) { it }
    }

    fun <A : Any, B : Any, C : Any> tripleNotNullColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (JdbcDialect, ResultSet) -> Triple<A, B, C> {
        return tripleColumns(expressions) {
            val first = checkNotNull(it.first) { "The value of the first column is null." }
            val second = checkNotNull(it.second) { "The value of the second column is null." }
            val third = checkNotNull(it.third) { "The value of the third column is null." }
            Triple(first, second, third)
        }
    }

    private fun <A : Any, B : Any, C : Any, AR, BR, CR> tripleColumns(
        expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        transform: (Triple<A?, B?, C?>) -> Triple<AR, BR, CR>
    ): (JdbcDialect, ResultSet) -> Triple<AR, BR, CR> =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            val third = mapper.execute(expressions.third)
            transform(Triple(first, second, third))
        }

    fun multipleColumns(expressions: List<ColumnExpression<*, *>>): (JdbcDialect, ResultSet) -> Record =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val map = expressions.associateWith { mapper.execute(it) }
            RecordImpl(map)
        }
}
