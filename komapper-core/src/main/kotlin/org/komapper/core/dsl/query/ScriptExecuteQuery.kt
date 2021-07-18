package org.komapper.core.dsl.query

import org.komapper.core.dsl.options.ScriptExecuteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface ScriptExecuteQuery : Query<Unit> {
    fun options(configure: (ScriptExecuteOptions) -> ScriptExecuteOptions): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    private val sql: String,
    private val options: ScriptExecuteOptions = ScriptExecuteOptions.default
) :
    ScriptExecuteQuery {

    override fun options(configure: (ScriptExecuteOptions) -> ScriptExecuteOptions): ScriptExecuteQuery {
        return copy(options = configure(options))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.scriptExecuteQuery(sql, options)
    }
}
