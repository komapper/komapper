package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
interface QueryContext {
    fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>>
    fun getCompositeWhere(): WhereDeclaration = {}
}
