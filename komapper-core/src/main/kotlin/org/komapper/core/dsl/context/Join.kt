package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Criterion
import org.komapper.core.metamodel.EntityMetamodel

internal data class Join<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val kind: JoinKind,
    val on: List<Criterion>
)
