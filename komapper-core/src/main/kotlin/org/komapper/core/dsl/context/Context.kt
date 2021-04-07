package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal interface Context<ENTITY : Any> {
    val entityMetamodel: EntityMetamodel<ENTITY>
    fun getEntityExpressions(): Set<EntityExpression<*>>
}
