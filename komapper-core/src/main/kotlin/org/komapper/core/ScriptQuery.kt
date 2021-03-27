package org.komapper.core

import org.komapper.core.query.Query
import org.komapper.core.query.ScriptQuery

object ScriptQuery {

    fun execute(sql: String): Query<Unit> {
        return ScriptQuery(sql)
    }
}
