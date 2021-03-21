package org.komapper.core.query

import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.data.SortItem

fun <E, T : Any> PropertyMetamodel<E, T>.desc(): PropertyMetamodel<E, T> {
    if (this is SortItem.Desc) {
        return this
    }
    return SortItem.Desc(this)
}

fun <E, T : Any> PropertyMetamodel<E, T>.asc(): PropertyMetamodel<E, T> {
    if (this is SortItem.Asc) {
        return this
    }
    return SortItem.Asc(this)
}

enum class ForUpdateOption {
    BASIC
}
