package org.komapper.core.dsl

import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.element.SortIndex
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expr.AggregateFunction
import org.komapper.core.dsl.expr.ArithmeticExpr
import org.komapper.core.dsl.expr.StringFunction
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.Column
import org.komapper.core.metamodel.IdGeneratorDescriptor
import org.komapper.core.metamodel.Table

fun Table.getName(mapper: (String) -> String): String {
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

fun Column<*>.getName(mapper: (String) -> String): String {
    return mapper(this.columnName)
}

fun <T : Any> Column<T>.desc(): Column<T> {
    if (this is SortItem.Desc) {
        return this
    }
    return SortItem.Desc(this)
}

fun desc(index: Number): Number {
    return SortIndex.Desc(index)
}

fun <T : Any> Column<T>.asc(): Column<T> {
    if (this is SortItem.Asc) {
        return this
    }
    return SortItem.Asc(this)
}

fun asc(index: Number): Number {
    return SortIndex.Asc(index)
}

fun <T : Any> avg(c: Column<T>): Column<Double> {
    return AggregateFunction.Avg(c)
}

fun count(): Column<Long> {
    return AggregateFunction.CountAsterisk
}

fun <T : Any> count(column: Column<T>): Column<Long> {
    return AggregateFunction.Count(column)
}

fun <T : Any> max(column: Column<T>): Column<T> {
    return AggregateFunction.Max(column)
}

fun <T : Any> min(column: Column<T>): Column<T> {
    return AggregateFunction.Min(column)
}

fun <T : Any> sum(column: Column<T>): Column<T> {
    return AggregateFunction.Sum(column)
}

infix operator fun <T : Number> Column<T>.plus(value: T): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Plus(this, left, right)
}

infix operator fun <T : Number> Column<T>.plus(other: Column<T>): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Plus(this, left, right)
}

infix operator fun <T : Number> Column<T>.minus(value: T): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Minus(this, left, right)
}

infix operator fun <T : Number> Column<T>.minus(other: Column<T>): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Minus(this, left, right)
}

infix operator fun <T : Number> Column<T>.times(value: T): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Times(this, left, right)
}

infix operator fun <T : Number> Column<T>.times(other: Column<T>): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Times(this, left, right)
}

infix operator fun <T : Number> Column<T>.div(value: T): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Div(this, left, right)
}

infix operator fun <T : Number> Column<T>.div(other: Column<T>): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Div(this, left, right)
}

infix operator fun <T : Number> Column<T>.rem(value: T): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpr.Rem(this, left, right)
}

infix operator fun <T : Number> Column<T>.rem(other: Column<T>): Column<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpr.Rem(this, left, right)
}

fun concat(left: Column<String>, right: String): Column<String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Parameter(left, right)
    return StringFunction.Concat(left, o1, o2)
}

fun concat(left: String, right: Column<String>): Column<String> {
    val o1 = Operand.Parameter(right, left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}

fun concat(left: Column<String>, right: Column<String>): Column<String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}
