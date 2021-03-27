package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Criterion

internal class JoinContext(
    internal val on: MutableList<Criterion> = mutableListOf()
) : Collection<Criterion> by on {

    fun add(criterion: Criterion) {
        on.add(criterion)
    }
}
