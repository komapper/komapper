package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.ScriptExecuteOptions

data class ScriptExecuteQueryRunner(
    private val sql: String,
    private val options: ScriptExecuteOptions = ScriptExecuteOptions.default
) :
    QueryRunner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement()
    }

    fun buildStatement(): Statement {
        return Statement(sql)
    }
}
