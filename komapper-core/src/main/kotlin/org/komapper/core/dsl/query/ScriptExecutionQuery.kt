package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.ScriptExecutionOptions
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.ScriptExecutionOptionsDeclaration
import org.komapper.core.dsl.scope.ScriptExecutionOptionsScope
import org.komapper.core.jdbc.JdbcExecutor

interface ScriptExecutionQuery : Query<Unit> {
    fun options(declaration: ScriptExecutionOptionsDeclaration): ScriptExecutionQuery
}

internal data class ScriptExecutionQueryImpl(val sql: String, val options: ScriptExecutionOptions = OptionsImpl()) :
    ScriptExecutionQuery {
    private val statement = Statement(sql, emptyList(), sql)

    override fun options(declaration: ScriptExecutionOptionsDeclaration): ScriptExecutionQueryImpl {
        val scope = ScriptExecutionOptionsScope(options)
        declaration(scope)
        return copy(options = scope.options)
    }

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config, options)
        return executor.execute(statement)
    }

    override fun toStatement(dialect: Dialect): Statement {
        return statement
    }
}
