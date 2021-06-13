package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.options.SqlSelectOptions

class SqlSelectQueryRunner(
    context: SqlSelectContext<*, *, *>,
    options: SqlSelectOptions,
) :
    QueryRunner {

    private val runner: SqlSelectFlowQueryRunner = SqlSelectFlowQueryRunner(context, options)

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        return runner.buildStatement(config)
    }
}
