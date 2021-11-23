package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SelectStatementBuilder
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.options.SelectOptions

class SelectRunner(
    private val context: SelectContext<*, *, *>,
    @Suppress("unused") private val options: SelectOptions,
) :
    Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        checkWhereClause(context, options)
        val builder = SelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
