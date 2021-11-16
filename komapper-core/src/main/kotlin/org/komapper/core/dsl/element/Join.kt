package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.declaration.OnDeclaration
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where

@ThreadSafe
data class Join<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val kind: JoinKind,
    val on: OnDeclaration
) {
    fun getWhereDeclarations(): List<WhereDeclaration> {
        return target.where
    }
}

enum class JoinKind {
    INNER, LEFT_OUTER
}
