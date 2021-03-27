package org.komapper.core.query

import org.komapper.core.query.context.SqlSelectContext

sealed class Projection {
    internal abstract val context: SqlSelectContext<*>

    internal data class Asterisk(
        override val context: SqlSelectContext<*>
    ) : Projection()

    internal data class SingleColumn(
        override val context: SqlSelectContext<*>
    ) : Projection()
}
