package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.data.Criterion

internal class JoinContext<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val kind: JoinKind,
    private val on: MutableList<Criterion> = mutableListOf()
) : Collection<Criterion> by on {

    fun add(criterion: Criterion) {
        on.add(criterion)
    }
}
