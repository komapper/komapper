package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.ScriptCommand

object ScriptQuery {
    fun execute(sql: String): Queryable<Unit> {
        return ScriptQueryableImpl(sql)
    }
}

private class ScriptQueryableImpl(sql: String) : Queryable<Unit> {
    private val statement = Statement(sql, emptyList(), sql)

    override fun run(config: DatabaseConfig) {
        val command = ScriptCommand(config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return statement
    }
}
