package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.option.QueryOption

internal class EntityInsertQuerySupport<ENTITY : Any>(
    private val context: EntityInsertContext<ENTITY>,
    private val option: QueryOption
) {

    fun preInsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        return if (assignment is Assignment.Sequence<ENTITY, *>) {
            assignment.assign(entity, config.name) {
                val sequenceName = assignment.getCanonicalSequenceName(config.dialect::enquote)
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
        val generatedKeysRequired = context.entityMetamodel.idAssignment() is Assignment.Identity<*, *>
        val executor = JdbcExecutor(config, option.asJdbcOption(), generatedKeysRequired)
        return execute(executor)
    }

    fun postInsert(entity: ENTITY, generatedKey: Long): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        return if (assignment is Assignment.Identity<ENTITY, *>) {
            assignment.assign(entity, generatedKey)
        } else {
            entity
        }
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityInsertStatementBuilder(config.dialect, context, entity)
        return builder.build()
    }
}
