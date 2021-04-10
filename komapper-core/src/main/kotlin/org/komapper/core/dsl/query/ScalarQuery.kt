package org.komapper.core.dsl.query

import org.komapper.core.dsl.expression.PropertyExpression

interface ScalarQuery<T, S : Any> : Subquery<T>, PropertyExpression<S>

internal data class ScalarQueryImpl<T, S : Any>(
    val query: Subquery<T>,
    val expression: PropertyExpression<S>
) :
    ScalarQuery<T, S>,
    Subquery<T> by query,
    PropertyExpression<S> by expression
