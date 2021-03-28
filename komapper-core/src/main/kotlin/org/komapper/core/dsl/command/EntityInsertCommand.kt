package org.komapper.core.dsl.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.util.getName
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel
import java.sql.PreparedStatement

internal class EntityInsertCommand<ENTITY>(
    @Suppress("unused") private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY,
    private val config: DatabaseConfig,
    private val statementBuilder: (Dialect, ENTITY) -> Statement
) : Command<ENTITY> {

    private val executor: JdbcExecutor = JdbcExecutor(config) { con, sql ->
        val assignment = entityMetamodel.idAssignment()
        if (assignment is Assignment.Identity<*, *>) {
            con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        } else {
            con.prepareStatement(sql)
        }
    }

    override fun execute(): ENTITY {
        val newEntity = preInsert()
        val statement = buildStatement(newEntity)
        return executor.executeUpdate(statement) { ps, _ ->
            postInsert(newEntity, ps)
        }
    }

    private fun preInsert(): ENTITY {
        val assignment = entityMetamodel.idAssignment()
        return if (assignment is Assignment.Sequence<ENTITY, *>) {
            val sequenceName = assignment.getName(config.dialect::quote)
            assignment.assign(entity, config.name) {
                val sql = config.dialect.getSequenceSql(sequenceName)
                val statement = Statement(sql)
                val executor = JdbcExecutor(config)
                executor.executeQuery(statement) { rs ->
                    if (rs.next()) rs.getLong(1) else error("No result: ${statement.sql}")
                }
            }
        } else {
            entity
        }.let { newEntity ->
            val clock = config.clockProvider.now()
            entityMetamodel.updateCreatedAt(newEntity, clock).let {
                entityMetamodel.updateUpdatedAt(it, clock)
            }
        }
    }

    private fun buildStatement(entity: ENTITY): Statement {
        return statementBuilder(config.dialect, entity)
    }

    private fun postInsert(entity: ENTITY, ps: PreparedStatement): ENTITY {
        val assignment = entityMetamodel.idAssignment()
        return if (assignment is Assignment.Identity<ENTITY, *>) {
            assignment.assign(entity) {
                ps.generatedKeys.use { rs ->
                    if (rs.next()) rs.getLong(1) else error("No result: Statement.generatedKeys")
                }
            }
        } else {
            entity
        }
    }
}
