package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.JdbcExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.option.ScriptExecuteOption

interface ScriptExecuteQuery : Query<Unit> {
    fun option(configurator: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    val sql: String,
    val option: ScriptExecuteOption = ScriptExecuteOption.default
) :
    ScriptExecuteQuery {
    private val statement = Statement(sql, emptyList(), sql)

    override fun option(configurator: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQueryImpl {
        return copy(option = configurator(option))
    }

    override fun run(holder: DatabaseConfigHolder) {
        val config = holder.config
        val executor = JdbcExecutor(config, option)
        return executor.execute(statement)
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        return statement.sql
    }
}
