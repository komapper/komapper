package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarExpression

fun avg(c: ColumnExpression<*, *>): ScalarExpression<Double, Double> {
    return AggregateFunction.Avg(c)
}

fun count(): ScalarExpression<Long, Long> {
    return AggregateFunction.CountAsterisk
}

fun count(property: ColumnExpression<*, *>): ScalarExpression<Long, Long> {
    return AggregateFunction.Count(property)
}

fun <T : Any, S : Any> max(property: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Max(property)
}

fun <T : Any, S : Any> min(property: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Min(property)
}

fun <T : Any, S : Any> sum(property: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Sum(property)
}
