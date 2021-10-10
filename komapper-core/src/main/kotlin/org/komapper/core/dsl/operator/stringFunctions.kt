package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.StringFunction

fun <T : Any> concat(
    left: ColumnExpression<T, String>,
    right: T
): ColumnExpression<T, String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Argument(left, right)
    return StringFunction.Concat(left, o1, o2)
}

fun <T : Any> concat(left: T, right: ColumnExpression<T, String>): ColumnExpression<T, String> {
    val o1 = Operand.Argument(right, left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}

fun <T : Any> concat(
    left: ColumnExpression<T, String>,
    right: ColumnExpression<T, String>
): ColumnExpression<T, String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}

fun <T : Any> lower(
    expression: ColumnExpression<T, String>
): ColumnExpression<T, String> {
    val operand = Operand.Column(expression)
    return StringFunction.Lower(expression, operand)
}

fun <T : Any> upper(
    expression: ColumnExpression<T, String>
): ColumnExpression<T, String> {
    val operand = Operand.Column(expression)
    return StringFunction.Upper(expression, operand)
}

fun <T : Any> substring(
    expression: ColumnExpression<T, String>,
    startIndex: Int,
    length: Int? = null
): ColumnExpression<T, String> {
    val target = Operand.Column(expression)
    val o1 = Operand.Argument(literal(startIndex), startIndex)
    val o2 = length?.let { Operand.Argument(literal(it), it) }
    return StringFunction.Substring(expression, target, o1, o2)
}

fun <T : Any> trim(
    expression: ColumnExpression<T, String>
): ColumnExpression<T, String> {
    val operand = Operand.Column(expression)
    return StringFunction.Trim(expression, operand)
}

fun <T : Any> ltrim(
    expression: ColumnExpression<T, String>
): ColumnExpression<T, String> {
    val operand = Operand.Column(expression)
    return StringFunction.Ltrim(expression, operand)
}

fun <T : Any> rtrim(
    expression: ColumnExpression<T, String>
): ColumnExpression<T, String> {
    val operand = Operand.Column(expression)
    return StringFunction.Rtrim(expression, operand)
}
