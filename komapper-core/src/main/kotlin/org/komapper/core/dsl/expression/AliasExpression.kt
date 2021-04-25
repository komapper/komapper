package org.komapper.core.dsl.expression

internal class AliasExpression<T : Any>(
    val expression: ColumnExpression<T>,
    val alias: String
) : ColumnExpression<T> by expression
