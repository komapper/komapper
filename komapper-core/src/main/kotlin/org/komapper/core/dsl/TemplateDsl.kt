package org.komapper.core.dsl

import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder
import org.komapper.core.dsl.query.TemplateSelectQueryBuilderImpl

/**
 * The entry point for constructing queries using sql templates.
 */
object TemplateDsl : Dsl {

    /**
     * Creates a builder for constructing a select query.
     *
     * @param sql the sql template
     * @return the builder
     */
    fun from(sql: String): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(TemplateSelectContext(sql))
    }

    /**
     * Creates a query for executing an arbitrary command.
     *
     * @param sql the sql template
     * @return the query
     */
    fun execute(sql: String): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(TemplateExecuteContext(sql))
    }
}
