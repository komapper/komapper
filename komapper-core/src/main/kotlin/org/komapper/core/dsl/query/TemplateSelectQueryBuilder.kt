package org.komapper.core.dsl.query

import org.komapper.core.dsl.option.TemplateSelectOption

interface TemplateSelectQueryBuilder {
    fun where(provide: () -> Any): TemplateSelectQueryBuilder
    fun option(configure: (TemplateSelectOption) -> TemplateSelectOption): TemplateSelectQueryBuilder
    fun <T> select(provide: Row.() -> T): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryBuilderImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateSelectOption = TemplateSelectOption.default
) : TemplateSelectQueryBuilder {

    override fun option(configure: (TemplateSelectOption) -> TemplateSelectOption): TemplateSelectQueryBuilder {
        return copy(option = configure(option))
    }

    override fun where(provide: () -> Any): TemplateSelectQueryBuilder {
        return copy(params = provide())
    }

    override fun <T> select(provide: Row.() -> T): TemplateSelectQuery<T> {
        return TemplateSelectQueryImpl(sql, params, provide, option)
    }
}
