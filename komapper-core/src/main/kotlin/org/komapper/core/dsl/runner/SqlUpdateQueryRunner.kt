package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlUpdateStatementBuilder
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlUpdateOptions

class SqlUpdateQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlUpdateContext<ENTITY, ID, META>,
    private val options: SqlUpdateOptions
) : QueryRunner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlUpdateStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
