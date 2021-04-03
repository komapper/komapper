package org.komapper.core.dsl

import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.Row
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryImpl

object TemplateQuery : Dsl {

    fun <T> select(
        sql: String,
        params: Any = object {},
        provider: Row.() -> T
    ): ListQuery<T> {
        return TemplateSelectQueryImpl(sql, params, provider)
    }

    fun execute(sql: String, params: Any = object {}): Query<Int> {
        return TemplateExecuteQueryImpl(sql, params)
    }
}
