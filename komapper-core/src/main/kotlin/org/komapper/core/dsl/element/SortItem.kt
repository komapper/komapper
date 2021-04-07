package org.komapper.core.dsl.element

import org.komapper.core.dsl.expr.PropertyExpression

internal sealed class SortItem {
    internal sealed class Property<T : Any> : PropertyExpression<T>, SortItem() {
        abstract val expression: PropertyExpression<T>

        data class Asc<T : Any>(override val expression: PropertyExpression<T>) :
            Property<T>(),
            PropertyExpression<T> by expression

        data class Desc<T : Any>(override val expression: PropertyExpression<T>) :
            Property<T>(),
            PropertyExpression<T> by expression
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
