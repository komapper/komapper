package org.komapper.core.dsl.expression

internal data class DistinctExpression<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
) : ColumnExpression<T, S> by expression
