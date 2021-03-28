package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext

interface SqlSubqueryResult {
    val contextHolder: ContextHolder
}

interface SingleColumnSqlSubqueryResult : SqlSubqueryResult

internal class SingleColumnSqlSubqueryResultImpl(
    override val contextHolder: ContextHolder
) : SingleColumnSqlSubqueryResult

data class ContextHolder internal constructor(
    internal val context: SqlSelectContext<*>
)
