package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class SqlInsertContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val values: Values<ENTITY> = Values.Declarations(emptyList())
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }
}
