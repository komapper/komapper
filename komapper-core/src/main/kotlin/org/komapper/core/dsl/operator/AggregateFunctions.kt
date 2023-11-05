package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.ColumnExpression

/**
 * Builds an AVG function.
 */
fun avg(expression: ColumnExpression<*, *>): AggregateFunction<Double, Double> {
    return AggregateFunction.Avg(expression)
}

/**
 * Builds an COUNT(*) function.
 */
fun count(): AggregateFunction<Long, Long> {
    return AggregateFunction.CountAsterisk
}

/**
 * Builds an COUNT(columnName) function.
 */
fun count(expression: ColumnExpression<*, *>): AggregateFunction<Long, Long> {
    return AggregateFunction.Count(expression)
}

/**
 * Builds a MAX function.
 */
fun <T : Any, S : Any> max(expression: ColumnExpression<T, S>): AggregateFunction<T, S> {
    return AggregateFunction.Max(expression)
}

/**
 * Builds a MIN function.
 */
fun <T : Any, S : Any> min(expression: ColumnExpression<T, S>): AggregateFunction<T, S> {
    return AggregateFunction.Min(expression)
}

/**
 * Builds a SUM function.
 */
fun <T : Any, S : Any> sum(expression: ColumnExpression<T, S>): AggregateFunction<T, S> {
    return AggregateFunction.Sum(expression)
}
