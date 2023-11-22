package org.komapper.core.dsl.expression

sealed interface ScalarExpression<T : Any, S : Any> : ColumnExpression<T, S>

internal data class ScalarAliasExpression<T : Any, S : Any>(
    val aliasExpression: AliasExpression<T, S>,
    val scalarExpression: ScalarExpression<T, S>,
) : ScalarExpression<T, S> by scalarExpression

internal data class ScalarArithmeticExpression<T : Any, S : Number>(
    val arithmeticExpression: ArithmeticExpression<T, S>,
    val scalarExpression: ScalarExpression<T, S>,
) : ScalarExpression<T, S> by scalarExpression
