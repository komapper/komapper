package org.komapper.core.dsl.query

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

    override fun <R> collect(transform: (Sequence<T>) -> R): Query<R> {
        return Collect(sql, params, provide, option, transform)
    }
    
    class Collect<T, R>(
        val sql: String,
        val params: Any,
        val provide: (Row) -> T,
        val option: TemplateSelectOption,
        val transform: (Sequence<T>) -> R
    ): Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}
