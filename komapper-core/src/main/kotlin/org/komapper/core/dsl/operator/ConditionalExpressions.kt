package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ConditionalExpression
import org.komapper.core.dsl.expression.When

/**
 * Builds a case expression.
 */
fun <T : Any, S : Any> case(
    firstWhen: When<T, S>,
    vararg remainingWhen: When<T, S>,
    otherwise: (() -> ColumnExpression<T, S>)? = null,
): ColumnExpression<T, S> {
    return ConditionalExpression.Case(firstWhen, remainingWhen.toList(), otherwise?.invoke())
}

/**
 * Builds a coalesce function.
 */
fun <T : Any, S : Any> coalesce(
    first: ColumnExpression<T, S>,
    second: ColumnExpression<T, S>,
    vararg expressions: ColumnExpression<T, S>,
): ColumnExpression<T, S> {
    return ConditionalExpression.Coalesce(first, second, expressions.toList())
}
