package org.komapper.core.dsl

import org.komapper.core.Database
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.AggregateFunction
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ArithmeticExpression
import org.komapper.core.dsl.expression.PropertyExpression
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

fun <T : Any> PropertyExpression<T>.asc(): PropertyExpression<T> {
    if (this is SortItem.Property.Asc) {
        return this
    }
    return SortItem.Property.Asc(this)
}

fun <T : Any> PropertyExpression<T>.desc(): PropertyExpression<T> {
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

infix fun <T : Any> PropertyExpression<T>.alias(alias: String): PropertyExpression<T> {
    return AliasExpression(this, alias)
}

fun <T : Any> avg(c: PropertyExpression<T>): ScalarExpression<Double> {
    return AggregateFunction.Avg(c)
}

fun count(): ScalarExpression<Long> {
    return AggregateFunction.CountAsterisk
}

fun <T : Any> count(property: PropertyExpression<T>): ScalarExpression<Long> {
    return AggregateFunction.Count(property)
}

fun <T : Any> max(property: PropertyExpression<T>): ScalarExpression<T> {
    return AggregateFunction.Max(property)
}

fun <T : Any> min(property: PropertyExpression<T>): ScalarExpression<T> {
    return AggregateFunction.Min(property)
}

fun <T : Any> sum(property: PropertyExpression<T>): ScalarExpression<T> {
    return AggregateFunction.Sum(property)
}

infix operator fun <T : Number> PropertyExpression<T>.plus(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.plus(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.minus(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.minus(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.times(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.times(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.div(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.div(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.rem(value: T): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Parameter(this, value)
    return ArithmeticExpression.Rem(this, left, right)
}

infix operator fun <T : Number> PropertyExpression<T>.rem(other: PropertyExpression<T>): PropertyExpression<T> {
    val left = Operand.Property(this)
    val right = Operand.Property(other)
    return ArithmeticExpression.Rem(this, left, right)
}

fun concat(left: PropertyExpression<String>, right: String): PropertyExpression<String> {
    val o1 = Operand.Property(left)
    val o2 = Operand.Parameter(left, right)
    return StringFunction.Concat(left, o1, o2)
}

fun concat(left: String, right: PropertyExpression<String>): PropertyExpression<String> {
    val o1 = Operand.Parameter(right, left)
    val o2 = Operand.Property(right)
    return StringFunction.Concat(right, o1, o2)
}

fun concat(left: PropertyExpression<String>, right: PropertyExpression<String>): PropertyExpression<String> {
    val o1 = Operand.Property(left)
    val o2 = Operand.Property(right)
    return StringFunction.Concat(right, o1, o2)
}
