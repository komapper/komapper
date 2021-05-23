package org.komapper.jdbc.dsl

import org.komapper.jdbc.dsl.query.TemplateExecuteQuery
import org.komapper.jdbc.dsl.query.TemplateExecuteQueryImpl
import org.komapper.jdbc.dsl.query.TemplateSelectQueryBuilder
import org.komapper.jdbc.dsl.query.TemplateSelectQueryBuilderImpl

object TemplateDsl : Dsl {

    fun from(sql: String): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(sql)
    }

    fun execute(sql: String): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(sql)
    }
}
