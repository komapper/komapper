package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

internal sealed class AggregateFunction<T : Any, S : Any> : ScalarExpression<T, S> {
    internal data class Avg(val expression: ColumnExpression<*, *>) : ColumnExpression<Double, Double>,
        AggregateFunction<Double, Double>() {
        override val owner: TableExpression<*> get() = expression.owner
        override val exteriorClass: KClass<Double> get() = Double::class
        override val interiorClass: KClass<Double> = Double::class
        override val columnName: String get() = expression.columnName
        override val alwaysQuote: Boolean get() = expression.alwaysQuote
        override val wrap: (Double) -> Double = { it }
        override val unwrap: (Double) -> Double = { it }
    }

    internal object CountAsterisk : ColumnExpression<Long, Long>, AggregateFunction<Long, Long>() {
        override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
        override val exteriorClass: KClass<Long> get() = Long::class
        override val interiorClass: KClass<Long> get() = Long::class
        override val columnName: String get() = throw UnsupportedOperationException()
        override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
        override val wrap: (Long) -> Long = { it }
        override val unwrap: (Long) -> Long = { it }
    }

    internal data class Count(val expression: ColumnExpression<*, *>) : ColumnExpression<Long, Long>,
        AggregateFunction<Long, Long>() {
        override val owner: TableExpression<*> get() = expression.owner
        override val exteriorClass: KClass<Long> get() = Long::class
        override val interiorClass: KClass<Long> = Long::class
        override val columnName: String get() = expression.columnName
        override val alwaysQuote: Boolean get() = expression.alwaysQuote
        override val wrap: (Long) -> Long = { it }
        override val unwrap: (Long) -> Long = { it }
    }

    internal data class Max<T : Any, S : Any>(val expression: ColumnExpression<T, S>) :
        ColumnExpression<T, S> by expression,
        AggregateFunction<T, S>()

    internal data class Min<T : Any, S : Any>(val expression: ColumnExpression<T, S>) :
        ColumnExpression<T, S> by expression,
        AggregateFunction<T, S>()

    internal data class Sum<T : Any, S : Any>(val expression: ColumnExpression<T, S>) :
        ColumnExpression<T, S> by expression,
        AggregateFunction<T, S>()
}
