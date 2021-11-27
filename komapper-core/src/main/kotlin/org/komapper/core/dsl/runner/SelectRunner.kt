package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SelectStatementBuilder
import org.komapper.core.dsl.context.SelectContext

class SelectRunner(
    private val context: SelectContext<*, *, *>,
) :
    Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        checkWhereClause(context)
        val builder = SelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
