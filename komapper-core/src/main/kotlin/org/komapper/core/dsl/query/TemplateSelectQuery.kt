package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.option.TemplateSelectOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

interface TemplateSelectQuery<T> : ListQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val params: Any,
    private val transform: (Row) -> T,
    private val option: TemplateSelectOption
) : TemplateSelectQuery<T> {

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.templateSelectQuery(sql, params, transform, option) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R> = Query { visitor ->
        visitor.templateSelectQuery(sql, params, transform, option, collect)
    }
}
