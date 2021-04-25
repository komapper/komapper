package org.komapper.core.dsl.expression

import org.komapper.core.dsl.element.Operand

internal sealed class ArithmeticExpression<T : Number> : ColumnExpression<T> {
    internal data class Plus<T : Number>(
        val expression: ColumnExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Minus<T : Number>(
        val expression: ColumnExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Times<T : Number>(
        val expression: ColumnExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Div<T : Number>(
        val expression: ColumnExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T> by expression, ArithmeticExpression<T>()
    internal data class Rem<T : Number>(
        val expression: ColumnExpression<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T> by expression, ArithmeticExpression<T>()
}
