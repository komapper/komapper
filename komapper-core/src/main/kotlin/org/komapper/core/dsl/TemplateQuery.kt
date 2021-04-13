package org.komapper.core.dsl

import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder
import org.komapper.core.dsl.query.TemplateSelectQueryBuilderImpl

object TemplateQuery : Dsl {

    fun from(sql: String): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(sql)
    }

    fun execute(sql: String): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(sql)
    }
}
