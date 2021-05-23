package org.komapper.core.dsl

import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ArithmeticExpression
import org.komapper.core.dsl.expression.BooleanExpression
import org.komapper.core.dsl.expression.CaseExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.IntExpression
import org.komapper.core.dsl.expression.LiteralExpression
import org.komapper.core.dsl.expression.LongExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.StringExpression
import org.komapper.core.dsl.expression.StringFunction
import org.komapper.core.dsl.expression.When

fun <T : Any, S : Any> ColumnExpression<T, S>.asc(): ColumnExpression<T, S> {
    if (this is SortItem.Property.Asc) {
        return this
    }
    return SortItem.Property.Asc(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.desc(): ColumnExpression<T, S> {
    if (this is SortItem.Property.Desc) {
        return this
    }
    return SortItem.Property.Desc(this)
}

fun asc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Asc(alias.toString())
}

fun desc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Desc(alias.toString())
}

infix fun <T : Any, S : Any> ColumnExpression<T, S>.alias(alias: String): ColumnExpression<T, S> {
    return AliasExpression(this, alias)
}

fun avg(c: ColumnExpression<*, *>): ScalarExpression<Double, Double> {
    return AggregateFunction.Avg(c)
}

fun count(): ScalarExpression<Long, Long> {
    return AggregateFunction.CountAsterisk
}

fun count(property: ColumnExpression<*, *>): ScalarExpression<Long, Long> {
    return AggregateFunction.Count(property)
}

fun <T : Any, S : Any> max(property: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Max(property)
}

fun <T : Any, S : Any> min(property: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Min(property)
}

fun <T : Any, S : Any> sum(property: ColumnExpression<T, S>): ScalarExpression<T, S> {
    return AggregateFunction.Sum(property)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.plus(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this, value)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.plus(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.minus(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this, value)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.minus(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.times(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this, value)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.times(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.div(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this, value)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.div(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.rem(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this, value)
    return ArithmeticExpression.Rem(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.rem(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Rem(this, left, right)
}

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

fun <T : Any, S : Any> case(
    firstWhen: When<T, S>,
    vararg remainingWhen: When<T, S>,
    otherwise: (() -> ColumnExpression<T, S>)? = null
): ColumnExpression<T, S> {
    return CaseExpression(firstWhen, remainingWhen.toList(), otherwise?.invoke())
}

fun literal(value: Boolean): ColumnExpression<Boolean, Boolean> {
    return LiteralExpression(value, BooleanExpression)
}

fun literal(value: Int): ColumnExpression<Int, Int> {
    return LiteralExpression(value, IntExpression)
}

fun literal(value: Long): ColumnExpression<Long, Long> {
    return LiteralExpression(value, LongExpression)
}

fun literal(value: String): ColumnExpression<String, String> {
    require("'" !in value) { "The value must not contain the single quotation." }
    return LiteralExpression(value, StringExpression)
}
