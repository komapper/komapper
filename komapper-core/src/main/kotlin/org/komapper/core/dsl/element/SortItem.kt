package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.ColumnExpression

@ThreadSafe
sealed class SortItem {
    sealed class Property<T : Any, S : Any> : ColumnExpression<T, S>, SortItem() {
        abstract val expression: ColumnExpression<T, S>

        data class Asc<T : Any, S : Any>(override val expression: ColumnExpression<T, S>) :
            Property<T, S>(),
            ColumnExpression<T, S> by expression

        data class Desc<T : Any, S : Any>(override val expression: ColumnExpression<T, S>) :
            Property<T, S>(),
            ColumnExpression<T, S> by expression
    }

    sealed class Alias : CharSequence, SortItem() {
        abstract val alias: String
        override val length get() = alias.length
        override fun get(index: Int) = alias[index]
        override fun subSequence(startIndex: Int, endIndex: Int) = alias.subSequence(startIndex, endIndex)

        data class Asc(override val alias: String) : Alias()
        data class Desc(override val alias: String) : Alias()
    }
}
