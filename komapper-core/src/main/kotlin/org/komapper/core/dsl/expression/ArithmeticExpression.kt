package org.komapper.core.dsl.expression

import org.komapper.core.dsl.element.Operand

internal sealed class ArithmeticExpression<T : Any, S : Number> : ColumnExpression<T, S> {
    internal data class Plus<T : Any, S : Number>(
        val expression: ColumnExpression<T, S>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T, S> by expression, ArithmeticExpression<T, S>()

    internal data class Minus<T : Any, S : Number>(
        val expression: ColumnExpression<T, S>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T, S> by expression, ArithmeticExpression<T, S>()

    internal data class Times<T : Any, S : Number>(
        val expression: ColumnExpression<T, S>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T, S> by expression, ArithmeticExpression<T, S>()

    internal data class Div<T : Any, S : Number>(
        val expression: ColumnExpression<T, S>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T, S> by expression, ArithmeticExpression<T, S>()

    internal data class Rem<T : Any, S : Number>(
        val expression: ColumnExpression<T, S>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T, S> by expression, ArithmeticExpression<T, S>()
}
