package org.komapper.core.dsl

import org.komapper.core.dsl.data.Operand
import org.komapper.core.dsl.data.SortItem
import org.komapper.core.dsl.expr.AggregateFunction
import org.komapper.core.dsl.expr.ArithmeticExpr
import org.komapper.core.dsl.expr.StringFunction
import org.komapper.core.metamodel.ColumnInfo

fun <T : Any> ColumnInfo<T>.desc(): ColumnInfo<T> {
    if (this is SortItem.Desc) {
        return this
    }
    return SortItem.Desc(this)
}

fun <T : Any> ColumnInfo<T>.asc(): ColumnInfo<T> {
    if (this is SortItem.Asc) {
        return this
    }
    return SortItem.Asc(this)
}

fun <T : Any> avg(c: ColumnInfo<T>): ColumnInfo<Double> {
    return AggregateFunction.Avg(c)
}

fun count(): ColumnInfo<Long> {
    return AggregateFunction.CountAsterisk
}

fun <T : Any> count(column: ColumnInfo<T>): ColumnInfo<Long> {
    return AggregateFunction.Count(column)
}

fun <T : Any> max(column: ColumnInfo<T>): ColumnInfo<T> {
    return AggregateFunction.Max(column)
}

fun <T : Any> min(column: ColumnInfo<T>): ColumnInfo<T> {
    return AggregateFunction.Min(column)
}

fun <T : Any> sum(column: ColumnInfo<T>): ColumnInfo<T> {
    return AggregateFunction.Sum(column)
}

infix operator fun <T : Number> ColumnInfo<T>.plus(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Plus(this, left, right)
}

infix operator fun <T : Number> T.plus(columnInfo: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Parameter(columnInfo, this)
    val right = Operand.Column(columnInfo)
    return ArithmeticExpr.Plus(columnInfo, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.minus(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Minus(this, left, right)
}

infix operator fun <T : Number> T.minus(columnInfo: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Parameter(columnInfo, this)
    val right = Operand.Column(columnInfo)
    return ArithmeticExpr.Minus(columnInfo, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.times(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Times(this, left, right)
}

infix operator fun <T : Number> T.times(columnInfo: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Parameter(columnInfo, this)
    val right = Operand.Column(columnInfo)
    return ArithmeticExpr.Times(columnInfo, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.div(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Div(this, left, right)
}

infix operator fun <T : Number> T.div(columnInfo: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Parameter(columnInfo, this)
    val right = Operand.Column(columnInfo)
    return ArithmeticExpr.Div(columnInfo, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.rem(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Rem(this, left, right)
}

infix operator fun <T : Number> T.rem(columnInfo: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Parameter(columnInfo, this)
    val right = Operand.Column(columnInfo)
    return ArithmeticExpr.Rem(columnInfo, left, right)
}

infix fun ColumnInfo<String>.concat(value: String): ColumnInfo<String> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return StringFunction.Concat(this, left, right)
}

infix fun String.concat(columnInfo: ColumnInfo<String>): ColumnInfo<String> {
    val left = Operand.Parameter(columnInfo, this)
    val right = Operand.Column(columnInfo)
    return StringFunction.Concat(columnInfo, left, right)
}
