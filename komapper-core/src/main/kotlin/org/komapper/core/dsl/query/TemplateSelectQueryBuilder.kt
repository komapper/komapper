package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.options.TemplateSelectOptions

@ThreadSafe
interface TemplateSelectQueryBuilder {
    fun where(provide: () -> Any): TemplateSelectQueryBuilder
    fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder
    fun <T> select(provide: (Row) -> T): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryBuilderImpl(
    private val sql: String,
    private val data: Any = object {},
    private val options: TemplateSelectOptions = TemplateSelectOptions.default
) : TemplateSelectQueryBuilder {

    override fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder {
        return copy(options = configure(options))
    }

    override fun where(provide: () -> Any): TemplateSelectQueryBuilder {
        return copy(data = provide())
    }

    override fun <T> select(provide: (Row) -> T): TemplateSelectQuery<T> {
        return TemplateSelectQueryImpl(sql, data, provide, options)
    }
}
