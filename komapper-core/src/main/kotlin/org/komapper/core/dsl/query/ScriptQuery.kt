package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor

internal data class ScriptQuery(val sql: String) : Query<Unit> {
    private val statement = Statement(sql, emptyList(), sql)

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config)
        return executor.execute(statement)
    }

    override fun toStatement(dialect: Dialect): Statement {
        return statement
    }
}
