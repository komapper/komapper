package org.komapper.core.dsl

import org.komapper.core.Database
import org.komapper.core.DatabaseConfig
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
    return block(QueryScope).run(this.config)
}

fun <T, R> Query<T>.flatMap(transform: (T) -> Query<R>): Query<R> {
    return object : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val result = this@flatMap.run(config)
            return transform(result).run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return this@flatMap.dryRun(config)
        }
    }
}

fun <T, R> Query<T>.flatZip(transform: (T) -> Query<R>): Query<Pair<T, R>> {
    return object : Query<Pair<T, R>> {
        override fun run(config: DatabaseConfig): Pair<T, R> {
            val result = this@flatZip.run(config)
            return result to transform(result).run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return this@flatZip.dryRun(config)
        }
    }
}

infix operator fun <T, S> Query<T>.plus(other: Query<S>): Query<S> {
    return object : Query<S> {
        override fun run(config: DatabaseConfig): S {
            this@plus.run(config)
            return other.run(config)
        }

        override fun dryRun(config: DatabaseConfig): String {
            return this@plus.dryRun(config) + other.dryRun(config)
        }
    }
}

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
    val right = Operand.ExteriorArgument(this, value)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.plus(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Plus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.minus(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.ExteriorArgument(this, value)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.minus(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Minus(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.times(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.ExteriorArgument(this, value)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.times(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Times(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.div(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.ExteriorArgument(this, value)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.div(other: ColumnExpression<T, S>): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.Column(other)
    return ArithmeticExpression.Div(this, left, right)
}

infix operator fun <T : Any, S : Number> ColumnExpression<T, S>.rem(value: T): ColumnExpression<T, S> {
    val left = Operand.Column(this)
    val right = Operand.ExteriorArgument(this, value)
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
    val o2 = Operand.ExteriorArgument(left, right)
    return StringFunction.Concat(left, o1, o2)
}

fun <T : Any> concat(left: T, right: ColumnExpression<T, String>): ColumnExpression<T, String> {
    val o1 = Operand.ExteriorArgument(right, left)
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
