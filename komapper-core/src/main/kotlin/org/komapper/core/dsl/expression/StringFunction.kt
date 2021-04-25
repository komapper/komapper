package org.komapper.core.dsl.expression

import org.komapper.core.dsl.element.Operand

internal sealed class StringFunction : ColumnExpression<String> {
    internal data class Concat(
        val expression: ColumnExpression<String>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<String> by expression, StringFunction()
}
