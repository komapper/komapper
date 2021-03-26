package org.komapper.core.query

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.query.data.SortItem

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

enum class ForUpdateOption {
    BASIC
}
