package org.komapper.core.dsl.expression

internal class AliasExpression<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
    val alias: String
) : ColumnExpression<T, S> by expression
