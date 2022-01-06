package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents the query to execute an arbitrary command using sql template.
 * This query returns the number of rows affected.
 */
interface TemplateExecuteQuery : Query<Int> {
    /**
     * Builds a query with the options applied.
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (TemplateExecuteOptions) -> TemplateExecuteOptions): TemplateExecuteQuery

    /**
     * Binds data to the query.
     * @param data data to be bound
     * @return the query
     */
    fun bind(data: Any): TemplateExecuteQuery
}

internal data class TemplateExecuteQueryImpl(
    private val context: TemplateExecuteContext,
) : TemplateExecuteQuery {

    override fun options(configure: (TemplateExecuteOptions) -> TemplateExecuteOptions): TemplateExecuteQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun bind(data: Any): TemplateExecuteQuery {
        val newContext = context.copy(data = data)
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.templateExecuteQuery(context)
    }
}
