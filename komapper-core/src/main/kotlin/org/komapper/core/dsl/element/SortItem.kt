package org.komapper.core.dsl.element

import org.komapper.core.dsl.expression.ColumnExpression

internal sealed class SortItem {
    internal sealed class Property<T : Any> : ColumnExpression<T>, SortItem() {
        abstract val expression: ColumnExpression<T>

        data class Asc<T : Any>(override val expression: ColumnExpression<T>) :
            Property<T>(),
            ColumnExpression<T> by expression

        data class Desc<T : Any>(override val expression: ColumnExpression<T>) :
            Property<T>(),
            ColumnExpression<T> by expression
    }

    internal sealed class Alias : CharSequence, SortItem() {
        abstract val alias: String
        override val length get() = alias.length
        override fun get(index: Int) = alias[index]
        override fun subSequence(startIndex: Int, endIndex: Int) = alias.subSequence(startIndex, endIndex)

        data class Asc(override val alias: String) : Alias()
        data class Desc(override val alias: String) : Alias()
    }
}
