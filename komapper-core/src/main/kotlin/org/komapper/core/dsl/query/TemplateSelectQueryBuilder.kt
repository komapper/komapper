package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.options.TemplateSelectOptions

@ThreadSafe
interface TemplateSelectQueryBuilder {
    fun bind(data: Any): TemplateSelectQueryBuilder
    fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder
    fun <T> select(transform: (Row) -> T): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryBuilderImpl(
    private val context: TemplateSelectContext,
) : TemplateSelectQueryBuilder {

    override fun options(configure: (TemplateSelectOptions) -> TemplateSelectOptions): TemplateSelectQueryBuilder {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun bind(data: Any): TemplateSelectQueryBuilder {
        val newContext = context.copy(data = data)
        return copy(context = newContext)
    }

    override fun <T> select(transform: (Row) -> T): TemplateSelectQuery<T> {
        return TemplateSelectQueryImpl(context, transform)
    }
}
