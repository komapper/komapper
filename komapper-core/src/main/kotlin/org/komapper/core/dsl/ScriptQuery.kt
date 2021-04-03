package org.komapper.core.dsl

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.ScriptExecuteQueryImpl

object ScriptQuery : Dsl {

    fun execute(sql: String): Query<Unit> {
        return ScriptExecuteQueryImpl(sql)
    }
}
