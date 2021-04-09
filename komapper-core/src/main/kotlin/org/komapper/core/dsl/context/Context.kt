package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression

internal interface Context {
    fun getEntityExpressions(): Set<EntityExpression<*>>
}
