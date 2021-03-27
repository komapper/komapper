package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.command.ScriptCommand

internal data class ScriptQuery(val sql: String) : Query<Unit> {
    private val statement = Statement(sql, emptyList(), sql)

    override fun run(config: DatabaseConfig) {
        val command = ScriptCommand(config, statement)
        return command.execute()
    }

    override fun peek(dialect: Dialect): Statement {
        return statement
    }
}
