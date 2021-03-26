package org.komapper.core.query

object TemplateQuery {

    fun <T> select(
        sql: String,
        params: Any = object {},
        provider: Row.() -> T
    ): TemplateSelectQueryable<T, List<T>> {
        return TemplateSelectQueryableImpl(sql, params, provider, { it.toList() })
    }

    fun <T, R> select(
        sql: String,
        params: Any = object {},
        provider: Row.() -> T,
        transformer: (Sequence<T>) -> R
    ): TemplateSelectQueryable<T, R> {
        return TemplateSelectQueryableImpl(sql, params, provider, transformer)
    }

    fun update(sql: String, params: Any = object {},): TemplateUpdateQueryable {
        return TemplateUpdateQueryableImpl(sql, params)
    }

    fun insert(sql: String, params: Any = object {},): TemplateInsertQueryable {
        return TemplateUpdateQueryableImpl(sql, params)
    }

    fun delete(sql: String, params: Any = object {},): TemplateDeleteQueryable {
        return TemplateUpdateQueryableImpl(sql, params)
    }
}
