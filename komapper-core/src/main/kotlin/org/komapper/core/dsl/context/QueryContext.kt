package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
interface QueryContext {
    fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>>
    fun getWhereDeclarations(): List<WhereDeclaration> = emptyList()
}
