package org.komapper.core.query.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor

internal class TemplateUpdateCommand(
    private val config: DatabaseConfig,
    private val statement: Statement
) : Command<Int> {

    private val executor: JdbcExecutor = JdbcExecutor(config)

    override fun execute(): Int {
        return executor.executeUpdate(statement) { _, count -> count }
    }
}
