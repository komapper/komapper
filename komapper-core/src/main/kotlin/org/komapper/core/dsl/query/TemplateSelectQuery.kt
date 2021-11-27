package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

interface TemplateSelectQuery<T> : ListQuery<T>, FlowQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val context: TemplateSelectContext,
    private val transform: (Row) -> T,
) : TemplateSelectQuery<T> {

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.templateSelectQuery(context, transform) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.templateSelectQuery(context, transform)
    }

    override fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.templateSelectQuery(context, transform, collect)
        }
    }
}
