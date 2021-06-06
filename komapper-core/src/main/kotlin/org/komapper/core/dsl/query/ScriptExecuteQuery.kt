package org.komapper.core.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.option.ScriptExecuteOption

interface ScriptExecuteQuery : Query<Unit> {
    fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQuery
}

data class ScriptExecuteQueryImpl(
    val sql: String,
    val option: ScriptExecuteOption = ScriptExecuteOption.default
) :
    ScriptExecuteQuery {
    private val statement = Statement(sql)

    override fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQueryImpl {
        return copy(option = configure(option))
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
