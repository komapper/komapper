package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.CaseExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.When

fun <T : Any, S : Any> case(
    firstWhen: When<T, S>,
    vararg remainingWhen: When<T, S>,
    otherwise: (() -> ColumnExpression<T, S>)? = null
): ColumnExpression<T, S> {
    return CaseExpression(firstWhen, remainingWhen.toList(), otherwise?.invoke())
}
