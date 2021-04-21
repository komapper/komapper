package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class EntityInsertContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META
) : Context {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target)
    }

    fun asEntityUpsertContext(duplicateKeyType: DuplicateKeyType): EntityUpsertContext<ENTITY, ID, META> {
        return EntityUpsertContext(target = target, duplicateKeyType = duplicateKeyType)
    }
}
