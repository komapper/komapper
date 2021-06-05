package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.dsl.option.TemplateSelectOption

interface TemplateSelectQuery<T> : ListQuery<T>

data class TemplateSelectQueryImpl<T>(
    val sql: String,
    val params: Any,
    val provide: (Row) -> T,
    val option: TemplateSelectOption
) : TemplateSelectQuery<T> {

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    override fun first(): Query<T> {
        return Collect(sql, params, provide, option) { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return Collect(sql, params, provide, option) { it.firstOrNull() }
    }

    override fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R> {
        return Collect(sql, params, provide, option, collect)
    }

    class Collect<T, R>(
        val sql: String,
        val params: Any,
        val provide: (Row) -> T,
        val option: TemplateSelectOption,
        val transform: suspend (Flow<T>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}
