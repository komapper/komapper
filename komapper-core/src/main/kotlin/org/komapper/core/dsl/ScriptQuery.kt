package org.komapper.core.dsl

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.ScriptExecutionQueryImpl

object ScriptQuery {

    fun execute(sql: String): Query<Unit> {
        return ScriptExecutionQueryImpl(sql)
    }
}
