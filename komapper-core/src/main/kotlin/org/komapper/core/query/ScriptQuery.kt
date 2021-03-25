package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.ScriptCommand

interface ScriptQuery : Query<Unit> {
    companion object {
        fun create(sql: String): ScriptQuery {
            return ScriptQueryImpl(sql)
        }
    }
}

internal class ScriptQueryImpl(private val sql: String) : ScriptQuery {
    private val statement = Statement(sql, emptyList(), sql)

    override fun run(config: DatabaseConfig) {
        val command = ScriptCommand(config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return statement
    }
}
