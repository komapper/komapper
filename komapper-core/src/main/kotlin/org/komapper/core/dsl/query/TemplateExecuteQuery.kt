package org.komapper.core.dsl.query

import org.komapper.core.dsl.option.TemplateExecuteOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

interface TemplateExecuteQuery : Query<Int> {
    fun option(configure: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQuery
    fun params(provide: () -> Any): TemplateExecuteQuery
}

internal data class TemplateExecuteQueryImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateExecuteOption = TemplateExecuteOption.default
) : TemplateExecuteQuery {

    override fun option(configure: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQueryImpl {
        return copy(option = configure(option))
    }

    override fun params(provide: () -> Any): TemplateExecuteQuery {
        return copy(params = provide())
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.templateExecuteQuery(sql, params, option)
    }
}
