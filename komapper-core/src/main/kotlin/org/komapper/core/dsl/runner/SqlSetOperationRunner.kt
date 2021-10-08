package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.DefaultAliasManager
import org.komapper.core.dsl.builder.SqlSetOperationStatementBuilder
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.options.SqlSetOperationOptions

class SqlSetOperationRunner(
    private val context: SqlSetOperationContext,
    @Suppress("unused") private val options: SqlSetOperationOptions,
) : Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val aliasManager = DefaultAliasManager(context)
        val builder = SqlSetOperationStatementBuilder(config.dialect, context, aliasManager)
        return builder.build()
    }
}
