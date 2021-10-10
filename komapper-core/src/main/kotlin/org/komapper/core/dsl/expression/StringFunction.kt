package org.komapper.core.dsl.expression

internal sealed class StringFunction<T : Any> : ColumnExpression<T, String> {
    internal data class Concat<T : Any>(
        val expression: ColumnExpression<T, String>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T>()

    internal data class Lower<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T>()

    internal data class Ltrim<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T>()

    internal data class Rtrim<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T>()

    internal data class Substring<T : Any>(
        val expression: ColumnExpression<T, String>,
        val target: Operand,
        val startIndex: Operand,
        val length: Operand?
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T>()

    internal data class Trim<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T>()

    internal data class Upper<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T>()
}
