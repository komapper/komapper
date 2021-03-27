package org.komapper.core.dsl.operand

import org.komapper.core.dsl.context.SqlSelectContext

interface SubqueryProjection {
    val contextHolder: ContextHolder
}

interface SingleColumnProjection : SubqueryProjection

internal class SingleColumnProjectionImpl(override val contextHolder: ContextHolder) : SingleColumnProjection

data class ContextHolder internal constructor(
    internal val context: SqlSelectContext<*>
)
