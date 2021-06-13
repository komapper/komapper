package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.options.SqlSelectOptions

class SqlSelectQueryRunner(
    private val context: SqlSelectContext<*, *, *>,
    @Suppress("unused") private val options: SqlSelectOptions,
) :
    QueryRunner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
