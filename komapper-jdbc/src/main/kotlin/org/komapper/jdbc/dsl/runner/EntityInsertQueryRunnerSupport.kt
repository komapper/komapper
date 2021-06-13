package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.runBlocking
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class EntityInsertQueryRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) {

    fun preInsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        val assignment = context.target.idAssignment()
        return if (!options.disableSequenceAssignment && assignment is Assignment.Sequence<ENTITY, *, *>) {
            runBlocking {
                assignment.assign(entity, config.id, config.dialect::enquote) { sequenceName ->
                    val sql = config.dialect.getSequenceSql(sequenceName)
                    val statement = Statement(sql)
                    val executor = JdbcExecutor(config, options)
                    executor.executeQuery(statement) { rs ->
                        if (rs.next()) rs.getLong(1) else error("No result: ${statement.fragments}")
                    }
                }
            }
        } else {
            entity
        }.let { newEntity ->
            val clock = config.clockProvider.now()
            context.target.preInsert(newEntity, clock)
        }
    }

    fun <T> insert(config: JdbcDatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val requiresGeneratedKeys = context.target.idAssignment() is Assignment.AutoIncrement<ENTITY, *, *>
        val executor = JdbcExecutor(config, options, requiresGeneratedKeys)
        return execute(executor)
    }

    fun postInsert(entity: ENTITY, generatedKey: Long): ENTITY {
        val assignment = context.target.idAssignment()
        return if (assignment is Assignment.AutoIncrement<ENTITY, *, *>) {
            assignment.assign(entity, generatedKey)
        } else {
            entity
        }
    }

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = EntityInsertStatementBuilder(config.dialect, context, entities)
        return builder.build()
    }
}
