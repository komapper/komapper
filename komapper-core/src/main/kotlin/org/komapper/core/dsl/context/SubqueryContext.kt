package org.komapper.core.dsl.context

sealed class SubqueryContext {
    internal data class EntitySelect(val context: EntitySelectContext<*>) : SubqueryContext()
    internal data class SqlSelect(val context: SqlSelectContext<*>) : SubqueryContext()
    internal data class SqlSetOperation(val context: SqlSetOperationContext<*>) : SubqueryContext()
}
