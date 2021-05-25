package org.komapper.r2dbc.dsl

import org.komapper.r2dbc.dsl.query.TemplateExecuteQuery
import org.komapper.r2dbc.dsl.query.TemplateExecuteQueryImpl
import org.komapper.r2dbc.dsl.query.TemplateSelectQueryBuilder
import org.komapper.r2dbc.dsl.query.TemplateSelectQueryBuilderImpl

object R2dbcTemplateDsl {

    fun from(sql: String): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(sql)
    }

    fun execute(sql: String): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(sql)
    }
}
