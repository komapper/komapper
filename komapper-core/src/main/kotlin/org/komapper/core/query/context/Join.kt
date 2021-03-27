package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.data.Criterion

internal data class Join<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val kind: JoinKind,
    val on: List<Criterion>
)
