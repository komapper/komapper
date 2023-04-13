package org.komapper.core.dsl.expression

internal class CoalesceExpression<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
    val expressions: List<ColumnExpression<T, S>>,
) : ColumnExpression<T, S> by expression
