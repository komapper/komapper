package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe

@ThreadSafe
sealed class SubqueryContext<T> {
    data class EntitySelect<T>(val context: EntitySelectContext<*, *, *>) : SubqueryContext<T>()
    data class SqlSelect<T>(val context: SqlSelectContext<*, *, *>) : SubqueryContext<T>()
    data class SqlSetOperation<T>(val context: SqlSetOperationContext<T>) : SubqueryContext<T>()
}
