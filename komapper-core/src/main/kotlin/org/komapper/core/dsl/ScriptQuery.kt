package org.komapper.core.dsl

import org.komapper.core.dsl.query.ScriptExecuteQuery
import org.komapper.core.dsl.query.ScriptExecuteQueryImpl

object ScriptQuery : Dsl {

    fun execute(sql: String): ScriptExecuteQuery {
        return ScriptExecuteQueryImpl(sql)
    }
}
