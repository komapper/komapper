package org.komapper.core.dsl.expression

sealed interface ScalarExpression<T : Any, S : Any> : ColumnExpression<T, S>

internal data class ScalarAliasExpression<T : Any, S : Any>(
    val expression: AliasExpression<T, S>,
) : ScalarExpression<T, S>, ColumnExpression<T, S> by expression

internal data class ScalarArithmeticExpression<T : Any, S : Number>(
    val expression: ArithmeticExpression<T, S>,
) : ScalarExpression<T, S>, ColumnExpression<T, S> by expression
