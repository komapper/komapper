package org.komapper.core.dsl.expression

import org.komapper.core.dsl.element.Operand

internal sealed class StringFunction<T : Any, S : CharSequence> : ColumnExpression<T, S> {
    internal data class Concat<T : Any, S : CharSequence>(
        val expression: ColumnExpression<T, S>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T, S> by expression, StringFunction<T, S>()
}
