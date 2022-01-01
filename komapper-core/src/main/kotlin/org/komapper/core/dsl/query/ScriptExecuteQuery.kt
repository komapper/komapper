package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.options.ScriptOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents the query to execute a script.
 * This query returns Unit.
 */
interface ScriptExecuteQuery : Query<Unit> {
    /**
     * Builds a query with the options applied.
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (ScriptOptions) -> ScriptOptions): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    private val context: ScriptContext,
) :
    ScriptExecuteQuery {

    override fun options(configure: (ScriptOptions) -> ScriptOptions): ScriptExecuteQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.scriptExecuteQuery(context)
    }
}
