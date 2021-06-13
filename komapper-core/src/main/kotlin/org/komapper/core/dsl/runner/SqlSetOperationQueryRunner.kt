package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.options.SqlSetOperationOptions

class SqlSetOperationQueryRunner(
    context: SqlSetOperationContext<*>,
    options: SqlSetOperationOptions,
) : QueryRunner {

    private val runner: SqlSetOperationFlowQueryRunner = SqlSetOperationFlowQueryRunner(context, options)

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
