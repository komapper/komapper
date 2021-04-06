package org.komapper.core.dsl.expr

import org.komapper.core.dsl.element.Operand

internal sealed class ArithmeticExpression<T : Number> : PropertyExpression<T> {
    internal data class Plus<T : Number>(
        val expression: PropertyExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        PropertyExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Minus<T : Number>(
        val expression: PropertyExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        PropertyExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Times<T : Number>(
        val expression: PropertyExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        PropertyExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Div<T : Number>(
        val expression: PropertyExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        PropertyExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Rem<T : Number>(
        val expression: PropertyExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        PropertyExpression<T> by expression, ArithmeticExpression<T>()
}
