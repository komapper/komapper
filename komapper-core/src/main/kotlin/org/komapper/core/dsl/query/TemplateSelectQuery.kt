package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface TemplateSelectQuery<T> : ListQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val data: Any,
    private val transform: (Row) -> T,
    private val options: TemplateSelectOptions
) : TemplateSelectQuery<T> {

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.templateSelectQuery(sql, data, transform, options) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.templateSelectQuery(sql, data, transform, options, collect)
        }
    }
}
