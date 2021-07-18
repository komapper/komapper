package org.komapper.core.dsl.query

import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface TemplateExecuteQuery : Query<Int> {
    fun options(configure: (TemplateExecuteOptions) -> TemplateExecuteOptions): TemplateExecuteQuery
    fun params(provide: () -> Any): TemplateExecuteQuery
}

internal data class TemplateExecuteQueryImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateExecuteOptions = TemplateExecuteOptions.default
) : TemplateExecuteQuery {

    override fun options(configure: (TemplateExecuteOptions) -> TemplateExecuteOptions): TemplateExecuteQuery {
        return copy(option = configure(option))
    }

    override fun params(provide: () -> Any): TemplateExecuteQuery {
        return copy(params = provide())
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.templateExecuteQuery(sql, params, option)
    }
}
