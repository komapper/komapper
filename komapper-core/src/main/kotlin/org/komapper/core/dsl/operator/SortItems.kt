package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem

/**
 * Builds an `asc` sort expression.
 */
fun <T : Any, S : Any> ColumnExpression<T, S>.asc(): SortExpression {
    return SortItem.Column.Asc(this)
}

/**
 * Builds an `asc nulls first` sort expression.
 */
fun <T : Any, S : Any> ColumnExpression<T, S>.ascNullsFirst(): SortExpression {
    return SortItem.Column.AscNullsFirst(this)
}

/**
 * Builds an `asc nulls last` sort expression.
 */
fun <T : Any, S : Any> ColumnExpression<T, S>.ascNullsLast(): SortExpression {
    return SortItem.Column.AscNullsLast(this)
}

/**
 * Builds an `desc` sort expression.
 */
fun <T : Any, S : Any> ColumnExpression<T, S>.desc(): SortExpression {
    return SortItem.Column.Desc(this)
}

/**
 * Builds an `desc nulls first` sort expression.
 */
fun <T : Any, S : Any> ColumnExpression<T, S>.descNullsFirst(): SortExpression {
    return SortItem.Column.DescNullsFirst(this)
}

/**
 * Builds an `desc nulls last` sort expression.
 */
fun <T : Any, S : Any> ColumnExpression<T, S>.descNullsLast(): SortExpression {
    return SortItem.Column.DescNullsLast(this)
}

/**
 * Builds an `asc` sort expression.
 */
fun asc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Asc(alias.toString())
}

/**
 * Builds an `asc nulls last` sort expression.
 */
fun ascNullsFirst(alias: CharSequence): CharSequence {
    return SortItem.Alias.AscNullsFirst(alias.toString())
}

/**
 * Builds an `asc nulls last` sort expression.
 */
fun ascNullsLast(alias: CharSequence): CharSequence {
    return SortItem.Alias.AscNullsLast(alias.toString())
}

/**
 * Builds an `desc` sort expression.
 */
fun desc(alias: CharSequence): CharSequence {
    return SortItem.Alias.Desc(alias.toString())
}

/**
 * Builds an `desc nulls first` sort expression.
 */
fun descNullsFirst(alias: CharSequence): CharSequence {
    return SortItem.Alias.DescNullsFirst(alias.toString())
}

/**
 * Builds an `desc nulls last` sort expression.
 */
fun descNullsLast(alias: CharSequence): CharSequence {
    return SortItem.Alias.DescNullsLast(alias.toString())
}
