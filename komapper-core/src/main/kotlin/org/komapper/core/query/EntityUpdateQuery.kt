package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.UpdateStatementBuilder
import org.komapper.core.query.command.EntityUpdateCommand
import org.komapper.core.query.context.UpdateContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

interface EntityUpdateQuery<ENTITY> : Query<ENTITY>

internal class EntityUpdateQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY
) :
    EntityUpdateQuery<ENTITY> {
    private val context: UpdateContext<ENTITY> = UpdateContext(entityMetamodel)

    override fun toSql(config: DefaultDatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    override fun run(config: DefaultDatabaseConfig): ENTITY {
        // TODO
        val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val newEntity = entityMetamodel.updateUpdatedAt(entity, clock)
        val builder = UpdateStatementBuilder(config, context, newEntity)
        val statement = builder.build()
        val command = EntityUpdateCommand(entityMetamodel, newEntity, config, statement)
        return command.execute()
    }

    private fun buildStatement(config: DefaultDatabaseConfig, entity: ENTITY): Statement {
        val builder = UpdateStatementBuilder(config, context, entity)
        return builder.build()
    }
}
