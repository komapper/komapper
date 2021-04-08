package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext

interface SqlSubqueryProjection {
    val contextHolder: ContextHolder
}

data class ContextHolder internal constructor(
    internal val context: SqlSelectContext<*>
)

interface OneProperty : SqlSubqueryProjection

interface TwoProperties : SqlSubqueryProjection

internal class SqlSubqueryProjectionImpl(
    override val contextHolder: ContextHolder
) : OneProperty, TwoProperties
