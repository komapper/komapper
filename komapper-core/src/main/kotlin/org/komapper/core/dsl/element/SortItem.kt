package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AscendingExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.DescendingExpression
import org.komapper.core.dsl.expression.SortExpression

@ThreadSafe
sealed class SortItem {
    sealed class Column : SortItem() {
        companion object {
            fun of(expression: SortExpression): Column {
                return when (expression) {
                    is AscendingExpression -> Asc(expression.column)
                    is DescendingExpression -> Desc(expression.column)
                    is ColumnExpression<*, *> -> Asc(expression)
                }
            }
        }
        data class Asc(val expression: ColumnExpression<*, *>) : Column()
        data class Desc(val expression: ColumnExpression<*, *>) : Column()
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
