package org.komapper.core.dsl.expr

import org.komapper.core.dsl.data.Operand
import org.komapper.core.metamodel.ColumnInfo

internal sealed class ArithmeticExpr<T : Number> : ColumnInfo<T> {
    internal data class Plus<T : Number>(
        val c: ColumnInfo<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnInfo<T> by c, ArithmeticExpr<T>()
    internal data class Minus<T : Number>(
        val c: ColumnInfo<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnInfo<T> by c, ArithmeticExpr<T>()
    internal data class Times<T : Number>(
        val c: ColumnInfo<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnInfo<T> by c, ArithmeticExpr<T>()
    internal data class Div<T : Number>(
        val c: ColumnInfo<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnInfo<T> by c, ArithmeticExpr<T>()
    internal data class Rem<T : Number>(
        val c: ColumnInfo<T>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnInfo<T> by c, ArithmeticExpr<T>()
}
