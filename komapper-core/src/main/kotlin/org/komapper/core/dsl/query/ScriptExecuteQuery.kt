package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.options.ScriptOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface ScriptExecuteQuery : Query<Unit> {
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
