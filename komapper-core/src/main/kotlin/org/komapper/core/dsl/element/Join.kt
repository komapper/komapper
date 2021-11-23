package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where

@ThreadSafe
data class Join<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val kind: JoinKind,
    val on: OnDeclaration
) {
    val where: WhereDeclaration get() = target.where
}

enum class JoinKind {
    INNER, LEFT_OUTER
}
