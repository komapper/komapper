package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.options.TemplateSelectOptions

@ThreadSafe
interface TemplateSelectQueryBuilder {
    fun bind(data: Any): TemplateSelectQueryBuilder
    fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder
    fun <T> select(transform: (Row) -> T): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryBuilderImpl(
    private val sql: String,
    private val data: Any = object {},
    private val options: TemplateSelectOptions = TemplateSelectOptions.default
) : TemplateSelectQueryBuilder {

    override fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder {
        return copy(options = configure(options))
    }

    override fun bind(data: Any): TemplateSelectQueryBuilder {
        return copy(data = data)
    }

    override fun <T> select(transform: (Row) -> T): TemplateSelectQuery<T> {
        return TemplateSelectQueryImpl(sql, data, transform, options)
    }
}
