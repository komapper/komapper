package org.komapper.core.query.data

import org.komapper.core.metamodel.ColumnInfo

internal sealed class SortItem<T : Any> : ColumnInfo<T> {
    abstract val columnInfo: ColumnInfo<T>

    data class Asc<T : Any>(override val columnInfo: ColumnInfo<T>) :
        SortItem<T>(),
        ColumnInfo<T> by columnInfo

    data class Desc<T : Any>(override val columnInfo: ColumnInfo<T>) :
        SortItem<T>(),
        ColumnInfo<T> by columnInfo
}
