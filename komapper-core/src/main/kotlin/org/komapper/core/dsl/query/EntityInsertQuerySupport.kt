package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.getName
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.Assignment

internal class EntityInsertQuerySupport<ENTITY>(
    private val context: EntityInsertContext<ENTITY>,
    private val option: EntityInsertOption
) {

    fun preInsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        return if (assignment is Assignment.Sequence<ENTITY, *>) {
            val sequenceName = assignment.getName(config.dialect::quote)
            assignment.assign(entity, config.name) {
                val sql = config.dialect.getSequenceSql(sequenceName)
                val statement = Statement(sql)
                val executor = JdbcExecutor(config, option.asJdbcOption())
                executor.executeQuery(statement) { rs ->
                    if (rs.next()) rs.getLong(1) else error("No result: ${statement.sql}")
                }
            }
        } else {
            entity
        }.let { newEntity ->
            val clock = config.clockProvider.now()
            context.entityMetamodel.updateCreatedAt(newEntity, clock).let {
                context.entityMetamodel.updateUpdatedAt(it, clock)
            }
        }
    }

    fun <T> insert(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, option.asJdbcOption()) { con, sql ->
            val assignment = context.entityMetamodel.idAssignment()
            if (assignment is Assignment.Identity<*, *>) {
                con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            } else {
                con.prepareStatement(sql)
            }
        }
        return execute(executor)
    }

    fun postInsert(entity: ENTITY, generatedKey: Long): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        return if (assignment is Assignment.Identity<ENTITY, *>) {
            assignment.assign(entity) { generatedKey }
        } else {
            entity
        }
    }

    fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        val builder = EntityInsertStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
