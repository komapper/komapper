package org.komapper.core.dsl

import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.query.ScriptExecuteQuery
import org.komapper.core.dsl.query.ScriptExecuteQueryImpl

/**
 * The entry point for constructing script queries.
 */
object ScriptDsl : Dsl {

    /**
     * Creates a query for executing a script.
     *
     * @param sql the script to execute
     */
    fun execute(sql: String): ScriptExecuteQuery {
        return ScriptExecuteQueryImpl(ScriptContext(sql))
    }
}
