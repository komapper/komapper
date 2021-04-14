package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityUpsertContext<ENTITY : Any>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val duplicateKeyType: DuplicateKeyType,
    val updateProperties: List<PropertyMetamodel<ENTITY, *>> = emptyList(),
) : Context {
    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
