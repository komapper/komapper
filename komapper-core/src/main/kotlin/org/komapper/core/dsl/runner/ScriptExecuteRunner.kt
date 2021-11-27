package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.ScriptContext

data class ScriptExecuteRunner(
    private val context: ScriptContext,
) :
    Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement()
    }

    fun buildStatement(): Statement {
        return Statement(context.sql)
    }
}
