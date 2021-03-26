package org.komapper.core.query.context

import org.komapper.core.query.data.SortItem

internal class OrderByContext(private val items: MutableList<SortItem<*>> = mutableListOf()) :
    Collection<SortItem<*>> by items {

    fun add(item: SortItem<*>) {
        items.add(item)
    }
}
