package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.ScriptExecuteOptions

data class ScriptExecuteQueryRunner(
    private val sql: String,
    private val options: ScriptExecuteOptions = ScriptExecuteOptions.default
) :
    QueryRunner {

    val statement = Statement(sql)

    override fun dryRun(config: DatabaseConfig): Statement {
        return statement
    }
}
