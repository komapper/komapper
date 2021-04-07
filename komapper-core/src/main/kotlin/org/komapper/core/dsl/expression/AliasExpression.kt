package org.komapper.core.dsl.expression

internal class AliasExpression<T : Any>(
    val expression: PropertyExpression<T>,
    val alias: String
) : PropertyExpression<T> by expression
