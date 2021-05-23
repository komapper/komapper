package org.komapper.core.dsl.expression

import org.komapper.core.dsl.context.SubqueryContext

interface SubqueryExpression<T> {
    val subqueryContext: SubqueryContext<T>
}
