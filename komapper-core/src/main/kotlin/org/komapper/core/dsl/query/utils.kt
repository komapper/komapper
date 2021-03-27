package org.komapper.core.dsl.query

import org.komapper.core.dsl.data.SortItem
import org.komapper.core.metamodel.ColumnInfo

fun <T : Any> ColumnInfo<T>.desc(): ColumnInfo<T> {
    if (this is SortItem.Desc) {
        return this
    }
    return SortItem.Desc(this)
}

fun <T : Any> ColumnInfo<T>.asc(): ColumnInfo<T> {
    if (this is SortItem.Asc) {
        return this
    }
    return SortItem.Asc(this)
}

fun <T : Any> avg(c: ColumnInfo<T>): ColumnInfo<Double> {
    return AggregateFunction.Avg(c)
}

fun count(): ColumnInfo<Long> {
    return AggregateFunction.CountAsterisk
}

fun <T : Any> count(column: ColumnInfo<T>): ColumnInfo<Long> {
    return AggregateFunction.Count(column)
}

fun <T : Any> max(column: ColumnInfo<T>): ColumnInfo<T> {
    return AggregateFunction.Max(column)
}

fun <T : Any> min(column: ColumnInfo<T>): ColumnInfo<T> {
    return AggregateFunction.Min(column)
}

fun <T : Any> sum(column: ColumnInfo<T>): ColumnInfo<T> {
    return AggregateFunction.Sum(column)
}
