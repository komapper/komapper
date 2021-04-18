package org.komapper.core.dsl.element

import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class Join<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    val entityMetamodel: EntityMetamodel<ENTITY, META>,
    val kind: JoinKind,
    val on: List<Criterion>
)

internal enum class JoinKind {
    INNER, LEFT_OUTER
}
