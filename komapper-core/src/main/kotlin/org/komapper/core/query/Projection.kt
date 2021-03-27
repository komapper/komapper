package org.komapper.core.query

import org.komapper.core.query.context.SqlSelectContext

interface Projection {
    val contextHolder: ContextHolder
}

interface SingleColumnProjection : Projection

internal class SingleColumnProjectionImpl(override val contextHolder: ContextHolder) : SingleColumnProjection

data class ContextHolder internal constructor(
    internal val context: SqlSelectContext<*>
)
