package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.ScriptContext

data class ScriptExecuteRunner(
    private val context: ScriptContext,
) :
    Runner {

    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = Statement(context.sql)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatements(): List<Statement> {
        return context.sql.split(context.options.separator)
            .asSequence()
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .map { Statement(it) }
            .toList()
    }
}
