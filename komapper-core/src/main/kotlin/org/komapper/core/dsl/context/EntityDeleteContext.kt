package org.komapper.core.dsl.context

import org.komapper.core.dsl.expr.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class EntityDeleteContext<ENTITY : Any>(
    override val entityMetamodel: EntityMetamodel<ENTITY>
) : Context<ENTITY> {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }
}
