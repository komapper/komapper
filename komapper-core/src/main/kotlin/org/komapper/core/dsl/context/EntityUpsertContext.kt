package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.UpdateSet
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class EntityUpsertContext<ENTITY : Any>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val duplicateKeyType: DuplicateKeyType,
    val updateSet: UpdateSet<ENTITY> =
        UpdateSet.Properties(
            entityMetamodel.properties()
                .filter { it != entityMetamodel.createdAtProperty() } - entityMetamodel.idProperties()
        ),
) : Context {
    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
