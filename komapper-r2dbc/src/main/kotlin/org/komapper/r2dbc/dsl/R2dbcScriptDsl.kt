package org.komapper.r2dbc.dsl

import org.komapper.r2dbc.dsl.query.ScriptExecuteQuery
import org.komapper.r2dbc.dsl.query.ScriptExecuteQueryImpl

object R2dbcScriptDsl {

    fun execute(sql: String): ScriptExecuteQuery {
        return ScriptExecuteQueryImpl(sql)
    }
}
