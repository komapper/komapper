package org.komapper.jdbc.dsl

import org.komapper.jdbc.dsl.query.ScriptExecuteQuery
import org.komapper.jdbc.dsl.query.ScriptExecuteQueryImpl

object ScriptDsl : Dsl {

    fun execute(sql: String): ScriptExecuteQuery {
        return ScriptExecuteQueryImpl(sql)
    }
}
