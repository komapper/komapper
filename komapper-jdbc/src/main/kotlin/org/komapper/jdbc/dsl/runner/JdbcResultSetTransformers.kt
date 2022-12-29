package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.RecordImpl
import org.komapper.jdbc.JdbcDataOperator
import java.sql.ResultSet

internal object JdbcResultSetTransformers {

    fun <T : Any> singleEntity(metamodel: EntityMetamodel<T, *, *>): (JdbcDataOperator, ResultSet) -> T =
        { dataOperator, rs ->
            val mapper = JdbcEntityMapper(dataOperator, rs)
            val entity = mapper.execute(metamodel, true)
            checkNotNull(entity)
        }

    fun <A : Any> singleColumn(expression: ColumnExpression<A, *>): (JdbcDataOperator, ResultSet) -> A? {
        return singleColumn(expression) { it }
    }

    fun <A : Any> singleNotNullColumn(expression: ColumnExpression<A, *>): (JdbcDataOperator, ResultSet) -> A {
        return singleColumn(expression) {
            checkNotNull(it) { "The value of the selected column is null." }
        }
    }

    private fun <A : Any, R> singleColumn(
        expression: ColumnExpression<A, *>,
        transform: (A?) -> R
    ): (JdbcDataOperator, ResultSet) -> R =
        { dataOperator, rs ->
            val extractor = JdbcValueExtractor(dataOperator, rs)
            val value = extractor.execute(expression)
            transform(value)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (JdbcDataOperator, ResultSet) -> Pair<A?, B?> {
        return pairColumns(expressions) { it }
    }

    fun <A : Any, B : Any> pairNotNullColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (JdbcDataOperator, ResultSet) -> Pair<A, B> {
        return pairColumns(expressions) {
            val first = checkNotNull(it.first) { "The value of the first column is null." }
            val second = checkNotNull(it.second) { "The value of the second column is null." }
            first to second
        }
    }

    private fun <A : Any, B : Any, AR, BR> pairColumns(
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        transform: (Pair<A?, B?>) -> Pair<AR, BR>
    ): (JdbcDataOperator, ResultSet) -> Pair<AR, BR> =
        { dataOperator, rs ->
            val extractor = JdbcValueExtractor(dataOperator, rs)
            val first = extractor.execute(expressions.first)
            val second = extractor.execute(expressions.second)
            transform(first to second)
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (JdbcDataOperator, ResultSet) -> Triple<A?, B?, C?> {
        return tripleColumns(expressions) { it }
    }

    fun <A : Any, B : Any, C : Any> tripleNotNullColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (JdbcDataOperator, ResultSet) -> Triple<A, B, C> {
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
    ): (JdbcDataOperator, ResultSet) -> Triple<AR, BR, CR> =
        { dataOperator, rs ->
            val extractor = JdbcValueExtractor(dataOperator, rs)
            val first = extractor.execute(expressions.first)
            val second = extractor.execute(expressions.second)
            val third = extractor.execute(expressions.third)
            transform(Triple(first, second, third))
        }

    fun multipleColumns(expressions: List<ColumnExpression<*, *>>): (JdbcDataOperator, ResultSet) -> Record =
        { dataOperator, rs ->
            val extractor = JdbcValueExtractor(dataOperator, rs)
            val map = expressions.associateWith { extractor.execute(it) }
            RecordImpl(map)
        }
}
