package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class EntityInsertContext<ENTITY : Any>(
    override val entityMetamodel: EntityMetamodel<ENTITY>
) : Context<ENTITY> {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }
}
