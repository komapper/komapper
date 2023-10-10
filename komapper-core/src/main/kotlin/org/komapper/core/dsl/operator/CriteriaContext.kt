package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.EscapeExpression
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

    /**
     * Does not escape the given string.
     */
    fun <S : CharSequence> text(value: S): EscapeExpression

    /**
     * Escapes the given string.
     */
    fun <S : CharSequence> escape(value: S): EscapeExpression
}
