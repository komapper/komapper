package org.komapper.core.dsl.element

import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class Join<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: EntityMetamodel<ENTITY, ID, META>,
    val kind: JoinKind,
    val on: List<Criterion>
)

internal enum class JoinKind {
    INNER, LEFT_OUTER
}
