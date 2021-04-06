package org.komapper.core.dsl.expr

import org.komapper.core.metamodel.Column
import org.komapper.core.metamodel.Table
import kotlin.reflect.KClass

internal sealed class AggregateFunction<T : Any> : Column<T> {
    internal data class Avg(val c: Column<*>) : Column<Double>, AggregateFunction<Double>() {
        override val owner: Table get() = c.owner
        override val klass: KClass<Double> get() = Double::class
        override val columnName: String get() = c.columnName
    }

    internal object CountAsterisk : Column<Long>, AggregateFunction<Long>() {
        override val owner: Table get() = throw UnsupportedOperationException()
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = throw UnsupportedOperationException()
    }

    internal data class Count(val c: Column<*>) : Column<Long>, AggregateFunction<Long>() {
        override val owner: Table get() = c.owner
        override val klass: KClass<Long> get() = Long::class
        override val columnName: String get() = c.columnName
    }

    internal data class Max<T : Any>(val c: Column<T>) : Column<T> by c, AggregateFunction<T>()
    internal data class Min<T : Any>(val c: Column<T>) : Column<T> by c, AggregateFunction<T>()
    internal data class Sum<T : Any>(val c: Column<T>) : Column<T> by c, AggregateFunction<T>()
}
