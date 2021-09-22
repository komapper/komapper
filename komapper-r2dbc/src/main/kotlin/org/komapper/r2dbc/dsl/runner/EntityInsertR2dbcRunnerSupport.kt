package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class EntityInsertR2dbcRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    val options: InsertOptions
) {

    suspend fun preInsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        val assignment = context.target.idAssignment()
        return if (!options.disableSequenceAssignment && assignment is Assignment.Sequence<ENTITY, *, *>) {
            assignment.assign(entity, config.id, config.dialect::enquote) { sequenceName ->
                val sql = config.dialect.getSequenceSql(sequenceName)
                val statement = Statement(sql)
                val executor = R2dbcExecutor(config, options)
                val flow = executor.executeQuery(statement) { _, row -> row.get(0) }
                when (val value = flow.firstOrNull() ?: error("No result: ${statement.toSql()}")) {
                    is Number -> value.toLong()
                    else -> error("The value class is not a Number. type=${value::class}")
                }
            }
        } else {
            entity
        }.let { newEntity ->
            val clock = config.clockProvider.now()
            context.target.preInsert(newEntity, clock)
        }
    }

    suspend fun <T> insert(config: R2dbcDatabaseConfig, execute: suspend (R2dbcExecutor) -> T): T {
        val generatedColumn = when (val assignment = context.target.idAssignment()) {
            is Assignment.AutoIncrement<ENTITY, *, *> -> assignment.columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, options, generatedColumn)
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
}
