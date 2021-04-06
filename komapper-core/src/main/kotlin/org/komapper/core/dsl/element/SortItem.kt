package org.komapper.core.dsl.element

import org.komapper.core.metamodel.Column

internal sealed class SortItem<T : Any> : Column<T> {
    abstract val column: Column<T>

    data class Asc<T : Any>(override val column: Column<T>) :
        SortItem<T>(),
        Column<T> by column

    data class Desc<T : Any>(override val column: Column<T>) :
        SortItem<T>(),
        Column<T> by column
}
