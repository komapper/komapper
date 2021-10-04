package org.komapper.core.dsl.operator

import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.AscendingExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.DescendingExpression
import org.komapper.core.dsl.expression.SortExpression

fun <T : Any, S : Any> ColumnExpression<T, S>.asc(): SortExpression {
    return AscendingExpression(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.desc(): SortExpression {
    return DescendingExpression(this)
}

fun asc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Asc(alias.toString())
}

fun desc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Desc(alias.toString())
}
