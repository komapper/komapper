package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SelectStatementBuilder
import org.komapper.core.dsl.context.SelectContext

class SelectRunner(
    private val context: SelectContext<*, *, *>,
) :
    Runner {
    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = buildStatement(config)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        checkWhereClause(context)
        val builder = SelectStatementBuilder(BuilderDialect(config), context)
        return builder.build()
    }
}
