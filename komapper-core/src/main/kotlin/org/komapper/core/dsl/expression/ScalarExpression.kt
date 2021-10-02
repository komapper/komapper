package org.komapper.core.dsl.expression

sealed interface ScalarExpression<T : Any, S : Any> : ColumnExpression<T, S>
