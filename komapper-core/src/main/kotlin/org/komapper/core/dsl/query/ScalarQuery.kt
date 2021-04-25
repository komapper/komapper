package org.komapper.core.dsl.query

import org.komapper.core.dsl.expression.ColumnExpression

interface ScalarQuery<T, S : Any> : Subquery<T>, ColumnExpression<S>

internal data class ScalarQueryImpl<T, S : Any>(
    val query: Subquery<T>,
    val expression: ColumnExpression<S>
) :
    ScalarQuery<T, S>,
    Subquery<T> by query,
    ColumnExpression<S> by expression
