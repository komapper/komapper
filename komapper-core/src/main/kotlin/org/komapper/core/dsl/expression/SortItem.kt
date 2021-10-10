package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe

@ThreadSafe
sealed class SortItem {
    sealed class Column : SortItem(), SortExpression {
        companion object {
            fun of(expression: SortExpression): Column {
                return when (expression) {
                    is Column -> expression
                    is ColumnExpression<*, *> -> Asc(expression)
                }
            }
        }
        abstract val expression: ColumnExpression<*, *>
        data class Asc(override val expression: ColumnExpression<*, *>) : Column()
        data class AscNullsFirst(override val expression: ColumnExpression<*, *>) : Column()
        data class AscNullsLast(override val expression: ColumnExpression<*, *>) : Column()
        data class Desc(override val expression: ColumnExpression<*, *>) : Column()
        data class DescNullsFirst(override val expression: ColumnExpression<*, *>) : Column()
        data class DescNullsLast(override val expression: ColumnExpression<*, *>) : Column()
    }

    sealed class Alias : CharSequence, SortItem() {
        abstract val alias: String
        override val length get() = alias.length
        override fun get(index: Int) = alias[index]
        override fun subSequence(startIndex: Int, endIndex: Int) = alias.subSequence(startIndex, endIndex)

        data class Asc(override val alias: String) : Alias()
        data class AscNullsFirst(override val alias: String) : Alias()
        data class AscNullsLast(override val alias: String) : Alias()
        data class Desc(override val alias: String) : Alias()
        data class DescNullsFirst(override val alias: String) : Alias()
        data class DescNullsLast(override val alias: String) : Alias()
    }
}
