package org.komapper.core.dsl

import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder
import org.komapper.core.dsl.query.TemplateSelectQueryBuilderImpl

object TemplateDsl : Dsl {

    fun from(sql: String): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(TemplateSelectContext(sql))
    }

    fun execute(sql: String): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(TemplateExecuteContext(sql))
    }
}
