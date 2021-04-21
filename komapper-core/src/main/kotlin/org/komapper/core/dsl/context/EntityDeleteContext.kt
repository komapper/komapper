package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class EntityDeleteContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META
) : Context {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target)
    }
}
