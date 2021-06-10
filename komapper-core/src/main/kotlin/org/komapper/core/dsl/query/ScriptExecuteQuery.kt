package org.komapper.core.dsl.query

import org.komapper.core.dsl.option.ScriptExecuteOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

interface ScriptExecuteQuery : Query<Unit> {
    fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    private val sql: String,
    private val option: ScriptExecuteOption = ScriptExecuteOption.default
) :
    ScriptExecuteQuery {

    override fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQueryImpl {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.scriptExecuteQuery(sql, option)
    }
}
