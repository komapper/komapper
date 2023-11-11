package org.komapper.core.dsl.expression

internal data class AliasExpression<T : Any, S : Any>(
    val expression: ColumnExpression<T, S>,
    val alias: String,
    val alwaysQuoteAlias: Boolean,
) : ColumnExpression<T, S> by expression
