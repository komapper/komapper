package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface TemplateExecuteQuery : Query<Int> {
    fun options(configure: (TemplateExecuteOptions) -> TemplateExecuteOptions): TemplateExecuteQuery
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
