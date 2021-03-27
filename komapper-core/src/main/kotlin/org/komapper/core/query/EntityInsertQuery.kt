package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Dialect
import org.komapper.core.jdbc.Executor
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.EntityInsertStatementBuilder
import org.komapper.core.query.command.EntityInsertCommand
import org.komapper.core.query.context.EntityInsertContext
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal data class EntityInsertQuery<ENTITY>(private val entityMetamodel: EntityMetamodel<ENTITY>, private val entity: ENTITY) :
    Query<ENTITY> {
    private val context: EntityInsertContext<ENTITY> = EntityInsertContext(entityMetamodel)

    override fun run(config: DatabaseConfig): ENTITY {
        val assignment = entityMetamodel.idAssignment()
        val newEntity = if (assignment is Assignment.Sequence<ENTITY, *>) {
            val sequenceName = assignment.name
            assignment.assign(entity, config.name) {
                val sql = config.dialect.getSequenceSql(sequenceName)
                val statement = Statement(sql)
                val executor = Executor(config)
                executor.executeQuery(statement) { rs ->
                    // TODO
                    if (rs.next()) rs.getLong(1) else error("no result")
                }
            }
        } else {
            entity
        }.let { newEntity ->
            val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
            entityMetamodel.updateCreatedAt(newEntity, clock).let {
                entityMetamodel.updateUpdatedAt(it, clock)
            }
        }
        val statement = buildStatement(config.dialect, newEntity)
        val command = EntityInsertCommand(entityMetamodel, newEntity, config, statement)
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
