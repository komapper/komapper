package org.komapper.core.dsl.query

import org.komapper.core.dsl.expression.ColumnExpression

interface ScalarQuery<A, B : Any, C : Any> : Subquery<A>, ColumnExpression<B, C>

internal data class ScalarQueryImpl<A, B : Any, C : Any>(
    val query: Subquery<A>,
    val expression: ColumnExpression<B, C>
) :
    ScalarQuery<A, B, C>,
    Subquery<A> by query,
    ColumnExpression<B, C> by expression
