package org.komapper.jdbc.h2

import org.komapper.core.dsl.context.Context
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityMergeContext<ENTITY : Any>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val on: List<PropertyMetamodel<ENTITY, *>> = entityMetamodel.idProperties(),
) : Context {
    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }
}
