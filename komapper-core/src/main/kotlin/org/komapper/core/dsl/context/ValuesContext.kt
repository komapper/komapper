package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SelectOptions

@ThreadSafe
data class ValuesContext(
    val rows: List<List<ColumnExpression<*, *>>>,
    override val options: SelectOptions = SelectOptions.DEFAULT,
) : SubqueryContext {
    fun getProjection(): Projection {
        return Projection.Expressions(rows.first())
    }
}
