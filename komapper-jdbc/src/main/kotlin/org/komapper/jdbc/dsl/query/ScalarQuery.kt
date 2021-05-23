package org.komapper.jdbc.dsl.query

import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.ScalarQueryExpression

interface ScalarQuery<A, B : Any, C : Any> : Subquery<A>, ScalarQueryExpression<A, B, C>

internal data class ScalarQueryImpl<A, B : Any, C : Any>(
    val query: Subquery<A>,
    val expression: ScalarExpression<B, C>
) :
    ScalarQuery<A, B, C>,
    Subquery<A> by query,
    ScalarExpression<B, C> by expression
