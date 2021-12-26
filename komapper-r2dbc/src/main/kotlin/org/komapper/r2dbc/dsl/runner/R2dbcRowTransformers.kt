package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.ColumnsImpl
import org.komapper.r2dbc.R2dbcDialect

internal object R2dbcRowTransformers {

    fun <ENTITY : Any> singleEntity(metamodel: EntityMetamodel<ENTITY, *, *>): (R2dbcDialect, Row) -> ENTITY =
        { dialect, row ->
            val mapper = R2dbcEntityMapper(dialect, row)
            mapper.execute(metamodel, true) as ENTITY
        }

    fun <A : Any> singleColumn(expression: ColumnExpression<A, *>): (R2dbcDialect, Row) -> A? {
        return singleColumn(expression) { it }
    }

    fun <A : Any> singleNotNullColumn(expression: ColumnExpression<A, *>): (R2dbcDialect, Row) -> A {
        return singleColumn(expression) {
            checkNotNull(it) { "The value of the selected column is null." }
        }
    }

    private fun <A : Any, R> singleColumn(expression: ColumnExpression<A, *>, transform: (A?) -> R): (R2dbcDialect, Row) -> R =
        { dialect, row ->
            val mapper = R2dbcPropertyMapper(dialect, row)
            val value = mapper.execute(expression)
            transform(value)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (R2dbcDialect, Row) -> Pair<A?, B?> =
        { dialect, row ->
            val mapper = R2dbcPropertyMapper(dialect, row)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            first to second
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (R2dbcDialect, Row) -> Triple<A?, B?, C?> =
        { dialect, row ->
            val mapper = R2dbcPropertyMapper(dialect, row)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            val third = mapper.execute(expressions.third)
            Triple(first, second, third)
        }

    fun multipleColumns(expressions: List<ColumnExpression<*, *>>): (R2dbcDialect, Row) -> Columns = { dialect, row ->
        val mapper = R2dbcPropertyMapper(dialect, row)
        val map = expressions.associateWith { mapper.execute(it) }
        ColumnsImpl(map)
    }
}
