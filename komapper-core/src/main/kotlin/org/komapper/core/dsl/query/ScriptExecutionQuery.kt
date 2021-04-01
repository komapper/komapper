package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.ScriptExecutionOptionDeclaration
import org.komapper.core.dsl.scope.ScriptExecutionOptionScope
import org.komapper.core.jdbc.JdbcExecutor

interface ScriptExecutionQuery : Query<Unit> {
    fun option(declaration: ScriptExecutionOptionDeclaration): ScriptExecutionQuery
}

internal data class ScriptExecutionQueryImpl(
    val sql: String,
    val option: ScriptExecutionOption = QueryOptionImpl()
) :
    ScriptExecutionQuery {
    private val statement = Statement(sql, emptyList(), sql)

    override fun option(declaration: ScriptExecutionOptionDeclaration): ScriptExecutionQueryImpl {
        val scope = ScriptExecutionOptionScope(option)
        declaration(scope)
        return copy(option = scope.option)
    }

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config, option.asJdbcOption())
        return executor.execute(statement)
    }

    override fun toStatement(dialect: Dialect): Statement {
        return statement
    }
}
