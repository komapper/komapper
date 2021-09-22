package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarExpression

fun avg(expression: ColumnExpression<*, *>): ScalarExpression<Double, Double> {
    return AggregateFunction.Avg(expression)
}

fun count(): ScalarExpression<Long, Long> {
    return AggregateFunction.CountAsterisk
}

fun count(expression: ColumnExpression<*, *>): ScalarExpression<Long, Long> {
    return AggregateFunction.Count(expression)
}

fun <T : Any, S : Any> max(expression: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Max(expression)
}

fun <T : Any, S : Any> min(expression: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Min(expression)
}

fun <T : Any, S : Any> sum(expression: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Sum(expression)
}
