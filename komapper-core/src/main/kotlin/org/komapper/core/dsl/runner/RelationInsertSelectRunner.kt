package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.RelationInsertSelectStatementBuilder
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class RelationInsertSelectRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertSelectContext<ENTITY, ID, META>,
) : Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = RelationInsertSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
