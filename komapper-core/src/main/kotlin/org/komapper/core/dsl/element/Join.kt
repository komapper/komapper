package org.komapper.core.dsl.element

import org.komapper.core.metamodel.EntityMetamodel

internal data class Join<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val kind: JoinKind,
    val on: List<Criterion>
)

internal enum class JoinKind {
    INNER, LEFT_OUTER
}
