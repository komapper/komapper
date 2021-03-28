package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.command.EntityUpdateCommand
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.metamodel.EntityMetamodel

internal data class EntityUpdateQuery<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY
) :
    Query<ENTITY> {
    private val context: EntityUpdateContext<ENTITY> = EntityUpdateContext(entityMetamodel)

    override fun run(config: DatabaseConfig): ENTITY {
        val command = EntityUpdateCommand(entityMetamodel, entity, config, this::buildStatement)
        return command.execute()
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect, entity)
    }

    private fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
