package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
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

    override fun run(config: DefaultDatabaseConfig) {
        val statement = Statement(sql, emptyList(), sql)
        val command = ScriptCommand(config, statement)
        return command.execute()
    }

    override fun toSql(config: DefaultDatabaseConfig): String {
        return sql
    }
}
