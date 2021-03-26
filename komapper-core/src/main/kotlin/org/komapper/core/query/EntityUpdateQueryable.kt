package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.EntityUpdateStatementBuilder
import org.komapper.core.query.command.EntityUpdateCommand
import org.komapper.core.query.context.EntityUpdateContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

interface EntityUpdateQueryable<ENTITY> : Queryable<ENTITY>

internal class EntityUpdateQueryableImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY
) :
    EntityUpdateQueryable<ENTITY> {
    private val context: EntityUpdateContext<ENTITY> = EntityUpdateContext(entityMetamodel)

    override fun run(config: DatabaseConfig): ENTITY {
        // TODO
        val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val newEntity = entityMetamodel.updateUpdatedAt(entity, clock)
        val builder = EntityUpdateStatementBuilder(config, context, newEntity)
        val statement = builder.build()
        val command = EntityUpdateCommand(entityMetamodel, newEntity, config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config, entity)
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(config, context, entity)
        return builder.build()
    }
}
