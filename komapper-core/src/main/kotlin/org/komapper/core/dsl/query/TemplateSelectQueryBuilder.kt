package org.komapper.core.dsl.query

import org.komapper.core.dsl.option.TemplateSelectOption

interface TemplateSelectQueryBuilder {
    fun where(provider: () -> Any): TemplateSelectQueryBuilder
    fun option(configurator: (TemplateSelectOption) -> TemplateSelectOption): TemplateSelectQueryBuilder
    fun <T> select(provider: Row.() -> T): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryBuilderImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateSelectOption = TemplateSelectOption.default
) : TemplateSelectQueryBuilder {

    override fun option(configurator: (TemplateSelectOption) -> TemplateSelectOption): TemplateSelectQueryBuilder {
        return copy(option = configurator(option))
    }

    override fun where(provider: () -> Any): TemplateSelectQueryBuilder {
        return copy(params = provider())
    }

    override fun <T> select(provider: Row.() -> T): TemplateSelectQuery<T> {
        return TemplateSelectQueryImpl(sql, params, provider, option)
    }
}
