package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Dialect
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.EntityDeleteStatementBuilder
import org.komapper.core.query.command.EntityDeleteCommand
import org.komapper.core.query.context.EntityDeleteContext

interface EntityDeleteQuery<ENTITY> : Query<Unit>

internal class EntityDeleteQueryImpl<ENTITY>(private val entityMetamodel: EntityMetamodel<ENTITY>, private val entity: ENTITY) :
    EntityDeleteQuery<ENTITY> {
    private val context: EntityDeleteContext<ENTITY> = EntityDeleteContext(entityMetamodel)

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config.dialect)
        val command = EntityDeleteCommand(entityMetamodel, entity, config, statement)
        command.execute()
    }

    override fun peek(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = EntityDeleteStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
