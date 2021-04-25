package org.komapper.core.dsl

import org.komapper.core.Database
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ArithmeticExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.StringFunction
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope

/**
 * Run a query.
 * @param block the Query provider
 */
fun <T> Database.runQuery(block: QueryScope.() -> Query<T>): T {
    return block(QueryScope).run(this)
}

fun <T, R> Query<T>.flatMap(transformer: (T) -> Query<R>): Query<R> {
    return object : Query<R> {
        override fun run(holder: DatabaseConfigHolder): R {
            val result = this@flatMap.run(holder)
            return transformer(result).run(holder)
        }

        override fun dryRun(holder: DatabaseConfigHolder): String {
            return this@flatMap.dryRun(holder)
        }
    }
}

fun <T, R> Query<T>.flatZip(transformer: (T) -> Query<R>): Query<Pair<T, R>> {
    return object : Query<Pair<T, R>> {
        override fun run(holder: DatabaseConfigHolder): Pair<T, R> {
            val result = this@flatZip.run(holder)
            return result to transformer(result).run(holder)
        }

        override fun dryRun(holder: DatabaseConfigHolder): String {
            return this@flatZip.dryRun(holder)
        }
    }
}

infix operator fun <T, S> Query<T>.plus(other: Query<S>): Query<S> {
    return object : Query<S> {
        override fun run(holder: DatabaseConfigHolder): S {
            this@plus.run(holder)
            return other.run(holder)
        }

        override fun dryRun(holder: DatabaseConfigHolder): String {
            return this@plus.dryRun(holder) + other.dryRun(holder)
        }
    }
}

fun <T : Any> ColumnExpression<T>.asc(): ColumnExpression<T> {
    if (this is SortItem.Property.Asc) {
        return this
    }
    return SortItem.Property.Asc(this)
}

fun <T : Any> ColumnExpression<T>.desc(): ColumnExpression<T> {
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

infix fun <T : Any> ColumnExpression<T>.alias(alias: String): ColumnExpression<T> {
    return AliasExpression(this, alias)
}

fun <T : Any> avg(c: ColumnExpression<T>): ScalarExpression<Double> {
    return AggregateFunction.Avg(c)
}

fun count(): ScalarExpression<Long> {
    return AggregateFunction.CountAsterisk
}

fun <T : Any> count(property: ColumnExpression<T>): ScalarExpression<Long> {
    return AggregateFunction.Count(property)
}

fun <T : Any> max(property: ColumnExpression<T>): ScalarExpression<T> {
    return AggregateFunction.Max(property)
}

fun <T : Any> min(property: ColumnExpression<T>): ScalarExpression<T> {
    return AggregateFunction.Min(property)
}

fun <T : Any> sum(property: ColumnExpression<T>): ScalarExpression<T> {
    return AggregateFunction.Sum(property)
}

infix operator fun <T : Number> ColumnExpression<T>.plus(value: T): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this.klass, value)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.plus(other: ColumnExpression<T>): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.minus(value: T): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this.klass, value)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.minus(other: ColumnExpression<T>): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.times(value: T): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this.klass, value)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.times(other: ColumnExpression<T>): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.div(value: T): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this.klass, value)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.div(other: ColumnExpression<T>): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.rem(value: T): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Argument(this.klass, value)
    return ArithmeticExpression.Rem(this, left, right)
}

infix operator fun <T : Number> ColumnExpression<T>.rem(other: ColumnExpression<T>): ColumnExpression<T> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Rem(this, left, right)
}

fun concat(left: ColumnExpression<String>, right: String): ColumnExpression<String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Argument(left.klass, right)
    return StringFunction.Concat(left, o1, o2)
}

fun concat(left: String, right: ColumnExpression<String>): ColumnExpression<String> {
    val o1 = Operand.Argument(right.klass, left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}

fun concat(left: ColumnExpression<String>, right: ColumnExpression<String>): ColumnExpression<String> {
    val o1 = Operand.Column(left)
    val o2 = Operand.Column(right)
    return StringFunction.Concat(right, o1, o2)
}
