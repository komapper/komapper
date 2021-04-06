package org.komapper.core.dsl.expr

import org.komapper.core.dsl.element.Operand
import org.komapper.core.metamodel.Column

internal sealed class ArithmeticExpr<T : Number> : Column<T> {
    internal data class Plus<T : Number>(
        val c: Column<T>,
        val left: Operand,
        val right: Operand
    ) :
        Column<T> by c, ArithmeticExpr<T>()
    internal data class Minus<T : Number>(
        val c: Column<T>,
        val left: Operand,
        val right: Operand
    ) :
        Column<T> by c, ArithmeticExpr<T>()
    internal data class Times<T : Number>(
        val c: Column<T>,
        val left: Operand,
        val right: Operand
    ) :
        Column<T> by c, ArithmeticExpr<T>()
    internal data class Div<T : Number>(
        val c: Column<T>,
        val left: Operand,
        val right: Operand
    ) :
        Column<T> by c, ArithmeticExpr<T>()
    internal data class Rem<T : Number>(
        val c: Column<T>,
        val left: Operand,
        val right: Operand
    ) :
        Column<T> by c, ArithmeticExpr<T>()
}
