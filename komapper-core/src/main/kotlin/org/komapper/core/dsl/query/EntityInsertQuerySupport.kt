package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.QueryOption

internal class EntityInsertQuerySupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val option: QueryOption
) {

    fun preInsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val assignment = context.target.idAssignment()
        return if (assignment is Assignment.Sequence<ENTITY, *>) {
            assignment.assign(entity, config.name, config.dialect::enquote) { sequenceName ->
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
            context.target.updateCreatedAt(newEntity, clock).let {
                context.target.updateUpdatedAt(it, clock)
            }
        }
    }

    fun <T> insert(config: DatabaseConfig, requiresGeneratedKeys: Boolean, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, option.asJdbcOption(), requiresGeneratedKeys)
        return execute(executor)
    }

    fun postInsert(entity: ENTITY, generatedKey: Long): ENTITY {
        val assignment = context.target.idAssignment()
        return if (assignment is Assignment.Identity<ENTITY, *>) {
            assignment.assign(entity, generatedKey)
        } else {
            entity
        }
    }
}
