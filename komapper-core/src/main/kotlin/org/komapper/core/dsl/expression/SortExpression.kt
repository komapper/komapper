package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe

@ThreadSafe
sealed interface SortExpression

internal class AscendingExpression(
    val column: ColumnExpression<*, *>
) : SortExpression

internal class DescendingExpression(
    val column: ColumnExpression<*, *>
) : SortExpression
