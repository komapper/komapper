package org.komapper.core.dsl.query

import org.komapper.core.metamodel.ColumnInfo
import kotlin.reflect.KClass

internal sealed class AggregateFunction {
    internal data class Avg(val c: ColumnInfo<*>) : ColumnInfo<Double>, AggregateFunction() {
        override val klass: KClass<Double> get() = Double::class
        override val columnName: String get() = c.columnName
    }

    internal object CountAsterisk : ColumnInfo<Long>, AggregateFunction() {
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = throw UnsupportedOperationException()
    }

    internal data class Count(val c: ColumnInfo<*>) : ColumnInfo<Long>, AggregateFunction() {
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = c.columnName
    }

    internal data class Max<T : Any>(val c: ColumnInfo<T>) : ColumnInfo<T> by c, AggregateFunction()
    internal data class Min<T : Any>(val c: ColumnInfo<T>) : ColumnInfo<T> by c, AggregateFunction()
    internal data class Sum<T : Any>(val c: ColumnInfo<T>) : ColumnInfo<T> by c, AggregateFunction()
}
