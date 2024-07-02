package org.komapper.core.dsl.expression

import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed class AggregateFunction<T : Any, S : Any> : ScalarExpression<T, S>, WindowFunction<T, S> {
    internal data class Avg(val expression: ColumnExpression<*, *>) : ColumnExpression<Double, Double>,
        AggregateFunction<Double, Double>() {
        override val owner: TableExpression<*> get() = expression.owner
        override val exteriorType: KType = typeOf<Double>()
        override val interiorType: KType = typeOf<Double>()
        override val columnName: String get() = expression.columnName
        override val alwaysQuote: Boolean get() = expression.alwaysQuote
        override val masking: Boolean get() = expression.masking
        override val wrap: (Double) -> Double = { it }
        override val unwrap: (Double) -> Double = { it }
    }

    internal object CountAsterisk : ColumnExpression<Long, Long>, AggregateFunction<Long, Long>() {
        override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
        override val exteriorType: KType = typeOf<Long>()
        override val interiorType: KType = typeOf<Long>()
        override val columnName: String get() = throw UnsupportedOperationException()
        override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
        override val masking: Boolean get() = throw UnsupportedOperationException()
        override val wrap: (Long) -> Long = { it }
        override val unwrap: (Long) -> Long = { it }
    }

    internal data class Count(val expression: ColumnExpression<*, *>) : ColumnExpression<Long, Long>,
        AggregateFunction<Long, Long>() {
        override val owner: TableExpression<*> get() = expression.owner
        override val exteriorType: KType = typeOf<Long>()
        override val interiorType: KType = typeOf<Long>()
        override val columnName: String get() = expression.columnName
        override val alwaysQuote: Boolean get() = expression.alwaysQuote
        override val masking: Boolean get() = expression.masking
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
