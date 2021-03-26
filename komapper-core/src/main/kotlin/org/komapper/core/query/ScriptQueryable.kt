package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.ScriptCommand

interface ScriptQueryable : Queryable<Unit> {
    companion object {
        fun create(sql: String): ScriptQueryable {
            return ScriptQueryableImpl(sql)
        }
    }
}

internal class ScriptQueryableImpl(private val sql: String) : ScriptQueryable {
    private val statement = Statement(sql, emptyList(), sql)

    override fun run(config: DatabaseConfig) {
        val command = ScriptCommand(config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return statement
    }
}
