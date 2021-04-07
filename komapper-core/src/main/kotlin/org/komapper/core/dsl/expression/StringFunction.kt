package org.komapper.core.dsl.expression

import org.komapper.core.dsl.element.Operand

internal sealed class StringFunction : PropertyExpression<String> {
    internal data class Concat(
        val expression: PropertyExpression<String>,
        val left: Operand,
        val right: Operand
    ) :
        PropertyExpression<String> by expression, StringFunction()
}
