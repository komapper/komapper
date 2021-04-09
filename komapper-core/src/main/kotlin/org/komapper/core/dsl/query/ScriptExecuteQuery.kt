package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.ScriptExecuteOptionScope
import org.komapper.core.dsl.scope.ScriptExecutionOptionDeclaration

interface ScriptExecuteQuery : Query<Unit> {
    fun option(declaration: ScriptExecutionOptionDeclaration): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    val sql: String,
    val option: ScriptExecutionOption = QueryOptionImpl()
) :
    ScriptExecuteQuery {
    private val statement = Statement(sql, emptyList(), sql)

    override fun option(declaration: ScriptExecutionOptionDeclaration): ScriptExecuteQueryImpl {
        val scope = ScriptExecuteOptionScope(option)
        declaration(scope)
        return copy(option = scope.option)
    }

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config, option.asJdbcOption())
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return statement
    }
}
