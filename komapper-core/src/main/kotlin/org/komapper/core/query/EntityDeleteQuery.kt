package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.DeleteStatementBuilder
import org.komapper.core.query.command.EntityDeleteCommand
import org.komapper.core.query.context.DeleteContext

interface EntityDeleteQuery<ENTITY> : Query<Unit>

internal class EntityDeleteQueryImpl<ENTITY>(private val entityMetamodel: EntityMetamodel<ENTITY>, private val entity: ENTITY) :
    EntityDeleteQuery<ENTITY> {
    private val context: DeleteContext<ENTITY> = DeleteContext(entityMetamodel)

    override fun run(config: DefaultDatabaseConfig) {
        val statement = buildStatement(config)
        val command = EntityDeleteCommand(entityMetamodel, entity, config, statement)
        command.execute()
    }

    override fun toSql(config: DefaultDatabaseConfig): String {
        val statement = buildStatement(config)
        return statement.sql
    }

    private fun buildStatement(config: DefaultDatabaseConfig): Statement {
        val builder = DeleteStatementBuilder(config, context, entity)
        return builder.build()
    }
}
