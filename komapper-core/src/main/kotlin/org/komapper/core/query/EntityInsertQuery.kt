package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.InsertStatementBuilder
import org.komapper.core.query.command.EntityInsertCommand
import org.komapper.core.query.context.InsertContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

interface EntityInsertQuery<ENTITY> : Query<ENTITY>

internal class EntityInsertQueryImpl<ENTITY>(private val entityMetamodel: EntityMetamodel<ENTITY>, private val entity: ENTITY) :
    EntityInsertQuery<ENTITY> {
    private val context: InsertContext<ENTITY> = InsertContext(entityMetamodel)

    override fun run(config: DefaultDatabaseConfig): ENTITY {
        val assignment = entityMetamodel.idAssignment()
        val newEntity = if (assignment is Assignment.Sequence<ENTITY, *>) {
            val sequenceName = assignment.name
            assignment.assign(entity, config.name) {
                val sql = config.dialect.getSequenceSql(sequenceName)
                val statement = Statement(sql)
                val executor = Executor(config)
                executor.executeQuery(statement) { rs ->
                    // TODO
                    if (rs.next()) listOf(rs.getLong(1)) else error("no result")
                }.first()
            }
        } else {
            entity
        }.let { newEntity ->
            val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
            entityMetamodel.updateCreatedAt(newEntity, clock).let {
                entityMetamodel.updateUpdatedAt(it, clock)
            }
        }
        val statement = buildStatement(config, newEntity)
        val command = EntityInsertCommand(entityMetamodel, newEntity, config, statement)
        return command.execute()
    }

    override fun toSql(config: DefaultDatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    private fun buildStatement(config: DefaultDatabaseConfig, entity: ENTITY): Statement {
        val builder = InsertStatementBuilder(config, context, entity)
        return builder.build()
    }
}
