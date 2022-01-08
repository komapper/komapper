package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to retrieve rows using sql template.
 * This query returns a list or flow that contains the retrieved rows.
 *
 * @param T the element type of [List] or [Flow]
 */
interface TemplateSelectQuery<T> : ListQuery<T>, FlowQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val context: TemplateSelectContext,
    private val transform: (Row) -> T,
) : TemplateSelectQuery<T> {

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.templateSelectQuery(context, transform)
    }

    override fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.templateSelectQuery(context, transform, collect)
        }
    }
}
