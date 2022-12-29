package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.RecordImpl
import org.komapper.r2dbc.R2dbcDataOperator

internal object R2dbcRowTransformers {

    fun <ENTITY : Any> singleEntity(metamodel: EntityMetamodel<ENTITY, *, *>): (R2dbcDataOperator, Row) -> ENTITY =
        { dataOperator, row ->
            val mapper = R2dbcEntityMapper(dataOperator, row)
            mapper.execute(metamodel, true) as ENTITY
        }

    fun <A : Any> singleColumn(expression: ColumnExpression<A, *>): (R2dbcDataOperator, Row) -> A? {
        return singleColumn(expression) { it }
    }

    fun <A : Any> singleNotNullColumn(expression: ColumnExpression<A, *>): (R2dbcDataOperator, Row) -> A {
        return singleColumn(expression) {
            checkNotNull(it) { "The value of the selected column is null." }
        }
    }

    private fun <A : Any, R> singleColumn(
        expression: ColumnExpression<A, *>,
        transform: (A?) -> R
    ): (R2dbcDataOperator, Row) -> R =
        { dataOperator, row ->
            val extractor = R2dbcValueExtractor(dataOperator, row)
            val value = extractor.execute(expression)
            transform(value)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (R2dbcDataOperator, Row) -> Pair<A?, B?> {
        return pairColumns(expressions) { it }
    }

    fun <A : Any, B : Any> pairNotNullColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (R2dbcDataOperator, Row) -> Pair<A, B> {
        return pairColumns(expressions) {
            val first = checkNotNull(it.first) { "The value of the first column is null." }
            val second = checkNotNull(it.second) { "The value of the second column is null." }
            first to second
        }
    }

    private fun <A : Any, B : Any, AR, BR> pairColumns(
        expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        transform: (Pair<A?, B?>) -> Pair<AR, BR>
    ): (R2dbcDataOperator, Row) -> Pair<AR, BR> =
        { dataOperator, row ->
            val extractor = R2dbcValueExtractor(dataOperator, row)
            val first = extractor.execute(expressions.first)
            val second = extractor.execute(expressions.second)
            transform(first to second)
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (R2dbcDataOperator, Row) -> Triple<A?, B?, C?> {
        return tripleColumns(expressions) { it }
    }

    fun <A : Any, B : Any, C : Any> tripleNotNullColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (R2dbcDataOperator, Row) -> Triple<A, B, C> {
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
    ): (R2dbcDataOperator, Row) -> Triple<AR, BR, CR> =
        { dataOperator, row ->
            val extractor = R2dbcValueExtractor(dataOperator, row)
            val first = extractor.execute(expressions.first)
            val second = extractor.execute(expressions.second)
            val third = extractor.execute(expressions.third)
            transform(Triple(first, second, third))
        }

    fun multipleColumns(expressions: List<ColumnExpression<*, *>>): (R2dbcDataOperator, Row) -> Record = { dataOperator, row ->
        val extractor = R2dbcValueExtractor(dataOperator, row)
        val map = expressions.associateWith { extractor.execute(it) }
        RecordImpl(map)
    }
}
