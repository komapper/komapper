package org.komapper.core.dsl.operator

import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.ColumnExpression

fun <T : Any, S : Any> ColumnExpression<T, S>.asc(): ColumnExpression<T, S> {
    if (this is SortItem.Property.Asc) {
        return this
    }
    return SortItem.Property.Asc(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.desc(): ColumnExpression<T, S> {
    if (this is SortItem.Property.Desc) {
        return this
    }
    return SortItem.Property.Desc(this)
}

fun asc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Asc(alias.toString())
}

fun desc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Desc(alias.toString())
}
