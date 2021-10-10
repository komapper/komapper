package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem

fun <T : Any, S : Any> ColumnExpression<T, S>.asc(): SortExpression {
    return SortItem.Column.Asc(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.ascNullsFirst(): SortExpression {
    return SortItem.Column.AscNullsFirst(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.ascNullsLast(): SortExpression {
    return SortItem.Column.AscNullsLast(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.desc(): SortExpression {
    return SortItem.Column.Desc(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.descNullsFirst(): SortExpression {
    return SortItem.Column.DescNullsFirst(this)
}

fun <T : Any, S : Any> ColumnExpression<T, S>.descNullsLast(): SortExpression {
    return SortItem.Column.DescNullsLast(this)
}

fun asc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Asc(alias.toString())
}

fun ascNullsFirst(alias: CharSequence): CharSequence {
    return SortItem.Alias.AscNullsFirst(alias.toString())
}

fun ascNullsLast(alias: CharSequence): CharSequence {
    return SortItem.Alias.AscNullsLast(alias.toString())
}

fun desc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Desc(alias.toString())
}

fun descNullsFirst(alias: CharSequence): CharSequence {
    return SortItem.Alias.DescNullsFirst(alias.toString())
}

fun descNullsLast(alias: CharSequence): CharSequence {
    return SortItem.Alias.DescNullsLast(alias.toString())
}
