package org.komapper.core.dsl.expression

import org.komapper.core.dsl.context.SubqueryContext

interface ScalarQueryExpression<A, B : Any, C : Any> : ScalarExpression<B, C> {
    val subqueryContext: SubqueryContext<A>
}
