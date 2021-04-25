package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

internal sealed class AggregateFunction<T : Any> : ScalarExpression<T> {
    internal data class Avg(val expression: ColumnExpression<*>) : ColumnExpression<Double>,
        AggregateFunction<Double>() {
        override val owner: TableExpression<*> get() = expression.owner
        override val klass: KClass<Double> get() = Double::class
        override val columnName: String get() = expression.columnName
        override val alwaysQuote: Boolean get() = expression.alwaysQuote
    }

    internal object CountAsterisk : ColumnExpression<Long>, AggregateFunction<Long>() {
        override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = throw UnsupportedOperationException()
        override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    }

    internal data class Count(val expression: ColumnExpression<*>) : ColumnExpression<Long>, AggregateFunction<Long>() {
        override val owner: TableExpression<*> get() = expression.owner
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = expression.columnName
        override val alwaysQuote: Boolean get() = expression.alwaysQuote
    }

    internal data class Max<T : Any>(val expression: ColumnExpression<T>) :
        ColumnExpression<T> by expression,
        AggregateFunction<T>()

    internal data class Min<T : Any>(val expression: ColumnExpression<T>) :
        ColumnExpression<T> by expression,
        AggregateFunction<T>()

    internal data class Sum<T : Any>(val expression: ColumnExpression<T>) :
        ColumnExpression<T> by expression,
        AggregateFunction<T>()
}
