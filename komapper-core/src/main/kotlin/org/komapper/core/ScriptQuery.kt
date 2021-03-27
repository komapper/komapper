package org.komapper.core

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.ScriptQuery

object ScriptQuery {

    fun execute(sql: String): Query<Unit> {
        return ScriptQuery(sql)
    }
}
