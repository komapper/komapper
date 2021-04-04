package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.ScriptExecutionOptionDeclaration
import org.komapper.core.dsl.scope.ScriptExecutionOptionScope
import org.komapper.core.jdbc.JdbcExecutor

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
        val scope = ScriptExecutionOptionScope(option)
        declaration(scope)
        return copy(option = scope.option)
    }

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config, option.asJdbcOption())
        return executor.execute(statement)
    }

    override fun dryRun(dialect: Dialect): Statement {
        return statement
    }
}
