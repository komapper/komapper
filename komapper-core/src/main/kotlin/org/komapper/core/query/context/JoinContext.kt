package org.komapper.core.query.context

import org.komapper.core.query.data.Criterion

internal class JoinContext(
    internal val on: MutableList<Criterion> = mutableListOf()
) : Collection<Criterion> by on {

    fun add(criterion: Criterion) {
        on.add(criterion)
    }
}
