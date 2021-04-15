package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class EntityInsertContext<ENTITY : Any>(
    val entityMetamodel: EntityMetamodel<ENTITY>
) : Context {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }

    fun asEntityUpsertContext(duplicateKeyType: DuplicateKeyType): EntityUpsertContext<ENTITY> {
        return EntityUpsertContext(entityMetamodel, duplicateKeyType)
    }
}
