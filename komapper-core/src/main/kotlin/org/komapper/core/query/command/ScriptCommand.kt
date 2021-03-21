package org.komapper.core.query.command

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor

internal class ScriptCommand(
    config: DefaultDatabaseConfig,
    override val statement: Statement
) : Command<Unit> {

    private val executor: Executor = Executor(config)

    override fun execute() {
        executor.execute(statement)
    }
}
