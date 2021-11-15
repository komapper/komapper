package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.declaration.OnDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
data class Join<ENTITY : Any, META : EntityMetamodel<ENTITY, *, META>>(
    val target: EntityMetamodel<ENTITY, *, META>,
    val kind: JoinKind,
    val on: OnDeclaration
)

enum class JoinKind {
    INNER, LEFT_OUTER
}
