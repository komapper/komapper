package org.komapper.core.query

object TemplateQuery {

    fun <T> select(
        sql: String,
        params: Any = object {},
        provider: Row.() -> T
    ): TemplateSelectQuery<T, List<T>> {
        return TemplateSelectQueryImpl(sql, params, provider, { it.toList() })
    }

    fun <T, R> select(
        sql: String,
        params: Any = object {},
        provider: Row.() -> T,
        transformer: (Sequence<T>) -> R
    ): TemplateSelectQuery<T, R> {
        return TemplateSelectQueryImpl(sql, params, provider, transformer)
    }

    fun update(sql: String, params: Any = object {},): TemplateUpdateQuery {
        return TemplateUpdateQueryImpl(sql, params)
    }

    fun insert(sql: String, params: Any = object {},): TemplateInsertQuery {
        return TemplateUpdateQueryImpl(sql, params)
    }

    fun delete(sql: String, params: Any = object {},): TemplateDeleteQuery {
        return TemplateUpdateQueryImpl(sql, params)
    }
}
