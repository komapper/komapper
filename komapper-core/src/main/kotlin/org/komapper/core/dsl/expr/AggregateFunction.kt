package org.komapper.core.dsl.expr

import kotlin.reflect.KClass

internal sealed class AggregateFunction<T : Any> : PropertyExpression<T> {
    internal data class Avg(val expression: PropertyExpression<*>) : PropertyExpression<Double>, AggregateFunction<Double>() {
        override val owner: EntityExpression<*> get() = expression.owner
        override val klass: KClass<Double> get() = Double::class
        override val columnName: String get() = expression.columnName
    }

    internal object CountAsterisk : PropertyExpression<Long>, AggregateFunction<Long>() {
        override val owner: EntityExpression<*> get() = throw UnsupportedOperationException()
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = throw UnsupportedOperationException()
    }

    internal data class Count(val expression: PropertyExpression<*>) : PropertyExpression<Long>, AggregateFunction<Long>() {
        override val owner: EntityExpression<*> get() = expression.owner
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = expression.columnName
    }

    internal data class Max<T : Any>(val expression: PropertyExpression<T>) : PropertyExpression<T> by expression, AggregateFunction<T>()
    internal data class Min<T : Any>(val expression: PropertyExpression<T>) : PropertyExpression<T> by expression, AggregateFunction<T>()
    internal data class Sum<T : Any>(val expression: PropertyExpression<T>) : PropertyExpression<T> by expression, AggregateFunction<T>()
}
