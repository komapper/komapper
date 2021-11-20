package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.DefaultAliasManager
import org.komapper.core.dsl.builder.SetOperationStatementBuilder
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.options.SelectOptions

class SetOperationRunner(
    private val context: SetOperationContext,
    @Suppress("unused") private val options: SelectOptions,
) : Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val aliasManager = DefaultAliasManager(context)
        val builder = SetOperationStatementBuilder(config.dialect, context, aliasManager)
        return builder.build()
    }
}
