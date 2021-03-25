package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.EntityDeleteStatementBuilder
import org.komapper.core.query.command.EntityDeleteCommand
import org.komapper.core.query.context.EntityDeleteContext

interface EntityDeleteQuery<ENTITY> : Query<Unit>

internal class EntityDeleteQueryImpl<ENTITY>(private val entityMetamodel: EntityMetamodel<ENTITY>, private val entity: ENTITY) :
    EntityDeleteQuery<ENTITY> {
    private val context: EntityDeleteContext<ENTITY> = EntityDeleteContext(entityMetamodel)

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val command = EntityDeleteCommand(entityMetamodel, entity, config, statement)
        command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = EntityDeleteStatementBuilder(config, context, entity)
        return builder.build()
    }
}
