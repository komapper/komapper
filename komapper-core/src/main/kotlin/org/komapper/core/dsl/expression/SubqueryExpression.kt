package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SubqueryContext

@ThreadSafe
interface SubqueryExpression<T> {
    val context: SubqueryContext
}
