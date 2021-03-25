package org.komapper.core.query

import org.komapper.core.query.context.EntitySelectContext
import org.komapper.core.query.context.SqlSelectContext

// TODO
sealed class SingleProjection {
    internal data class ContextHolder(val context: EntitySelectContext<*>) : SingleProjection()
    internal data class SqlSelectContextHolder(val context: SqlSelectContext<*>) : SingleProjection()
}
