package org.komapper.core.dsl.query

import org.komapper.core.dsl.option.TemplateExecuteOption

interface TemplateExecuteQuery : Query<Int> {
    fun option(configure: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQuery
    fun params(provide: () -> Any): TemplateExecuteQuery
}

data class TemplateExecuteQueryImpl(
    val sql: String,
    val params: Any = object {},
    val option: TemplateExecuteOption = TemplateExecuteOption.default
) : TemplateExecuteQuery {

    override fun option(configure: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQueryImpl {
        return copy(option = configure(option))
    }

    override fun params(provide: () -> Any): TemplateExecuteQuery {
        return copy(params = provide())
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
