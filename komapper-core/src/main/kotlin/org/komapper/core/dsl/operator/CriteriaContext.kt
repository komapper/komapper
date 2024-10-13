package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.SqlBuilderScope

/**
 * The context for criteria.
 */
interface CriteriaContext {
    /**
     * Adds a SQL builder.
     *
     * @param build the SQL builder
     */
    fun add(build: SqlBuilderScope.() -> Unit)
}
