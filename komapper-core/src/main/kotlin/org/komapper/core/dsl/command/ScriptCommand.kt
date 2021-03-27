package org.komapper.core.dsl.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor

internal class ScriptCommand(
    config: DatabaseConfig,
    private val statement: Statement
) : Command<Unit> {

    private val executor: JdbcExecutor = JdbcExecutor(config)

    override fun execute() {
        executor.execute(statement)
    }
}
