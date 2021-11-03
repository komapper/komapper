package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.options.EntitySelectOptions

class EntitySelectRunner(
    private val context: EntitySelectContext<*, *, *>,
    @Suppress("unused") private val options: EntitySelectOptions,
) : Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = EntitySelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
