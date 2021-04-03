package org.komapper.core.dsl

import org.komapper.core.dsl.data.Operand
import org.komapper.core.dsl.data.SortItem
import org.komapper.core.dsl.expr.AggregateFunction
import org.komapper.core.dsl.expr.ArithmeticExpr
import org.komapper.core.dsl.expr.StringFunction
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.IdGeneratorDescriptor
import org.komapper.core.metamodel.TableInfo

fun TableInfo.getName(mapper: (String) -> String): String {
    return listOf(this.catalogName(), this.schemaName(), this.tableName())
        .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
}

fun IdGeneratorDescriptor.Sequence<*, *>.getName(mapper: (String) -> String): String {
    return listOf(this.catalogName, this.schemaName, this.name)
        .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
}

fun Assignment.Sequence<*, *>.getName(mapper: (String) -> String): String {
    return listOf(this.catalogName, this.schemaName, this.name)
        .filter { it.isNotBlank() }.joinToString(".", transform = mapper)
}

fun ColumnInfo<*>.getName(mapper: (String) -> String): String {
    return mapper(this.columnName)
}

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

infix operator fun <T : Number> ColumnInfo<T>.plus(other: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Plus(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.minus(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Minus(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.minus(other: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Minus(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.times(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Times(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.times(other: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Times(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.div(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Div(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.div(other: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Div(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.rem(value: T): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Rem(this, left, right)
}

infix operator fun <T : Number> ColumnInfo<T>.rem(other: ColumnInfo<T>): ColumnInfo<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Rem(this, left, right)
}

fun concat(left: ColumnInfo<String>, right: String): ColumnInfo<String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Parameter(left, right)
    return StringFunction.Concat(left, o1, o2)
}

fun concat(left: String, right: ColumnInfo<String>): ColumnInfo<String> {
    val o1 = Operand.Parameter(right, left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}

fun concat(left: ColumnInfo<String>, right: ColumnInfo<String>): ColumnInfo<String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}
