package org.komapper.core.dsl.context

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityInsertContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META
) : Context {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }

    fun asEntityUpsertContext(
        keys: List<PropertyMetamodel<ENTITY, *>>,
        duplicateKeyType: DuplicateKeyType
    ): EntityUpsertContext<ENTITY, ID, META> {
        return EntityUpsertContext(
            target = target,
            keys = keys.ifEmpty { target.idProperties() },
            duplicateKeyType = duplicateKeyType
        )
    }
}
