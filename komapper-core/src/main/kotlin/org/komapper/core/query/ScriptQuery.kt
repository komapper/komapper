package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Dialect
import org.komapper.core.query.command.ScriptCommand

object ScriptQuery {
    fun execute(sql: String): Query<Unit> {
        return ScriptQueryImpl(sql)
    }
}

private class ScriptQueryImpl(sql: String) : Query<Unit> {
    private val statement = Statement(sql, emptyList(), sql)

    override fun run(config: DatabaseConfig) {
        val command = ScriptCommand(config, statement)
        return command.execute()
    }

    override fun peek(dialect: Dialect): Statement {
        return statement
    }
}
