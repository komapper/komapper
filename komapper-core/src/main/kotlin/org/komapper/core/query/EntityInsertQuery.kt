package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.EntityInsertStatementBuilder
import org.komapper.core.query.command.EntityInsertCommand
import org.komapper.core.query.context.EntityInsertContext

internal data class EntityInsertQuery<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY
) :
    Query<ENTITY> {
    private val context: EntityInsertContext<ENTITY> = EntityInsertContext(entityMetamodel)

    override fun run(config: DatabaseConfig): ENTITY {
        val command = EntityInsertCommand(entityMetamodel, entity, config, this::buildStatement)
        return command.execute()
    }

    override fun peek(dialect: Dialect): Statement {
        return buildStatement(dialect, entity)
    }

    private fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        val builder = EntityInsertStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
