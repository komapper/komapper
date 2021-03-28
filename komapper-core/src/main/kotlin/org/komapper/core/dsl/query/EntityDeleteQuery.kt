package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.command.EntityDeleteCommand
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.metamodel.EntityMetamodel

internal data class EntityDeleteQuery<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY
) :
    Query<Unit> {
    private val context: EntityDeleteContext<ENTITY> = EntityDeleteContext(entityMetamodel)

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config.dialect)
        val command = EntityDeleteCommand(entityMetamodel, entity, config, statement)
        command.execute()
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = EntityDeleteStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
