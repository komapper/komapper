package org.komapper.core.dsl.context

sealed class SubqueryContext<T> {
    internal data class EntitySelect<T>(val context: EntitySelectContext<*>) : SubqueryContext<T>()
    internal data class SqlSelect<T>(val context: SqlSelectContext<*>) : SubqueryContext<T>()
    internal data class SqlSetOperation<T>(val context: SqlSetOperationContext<T>) : SubqueryContext<T>()
}
