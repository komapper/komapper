package org.komapper.core.dsl.context

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityInsertContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }

    fun asEntityUpsertContext(
        keys: List<PropertyMetamodel<ENTITY, *, *>>,
        duplicateKeyType: DuplicateKeyType
    ): EntityUpsertContext<ENTITY, ID, META> {
        return EntityUpsertContext(
            insertContext = this,
            target = target,
            keys = keys.ifEmpty { target.idProperties() },
            duplicateKeyType = duplicateKeyType
        )
    }

    fun asSqlInsertContext(): SqlInsertContext<ENTITY, ID, META> {
        return SqlInsertContext(
            target = target
        )
    }
}
