package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class EntityInsertR2dbcRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    val options: InsertOptions
) {

    suspend fun preInsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        val newEntity = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.Sequence<ENTITY, ID> -> {
                if (!context.target.disableSequenceAssignment() && !options.disableSequenceAssignment) {
                    val id = idGenerator.generate(config.id, config.dialect::enquote) { sequenceName ->
                        val sql = config.dialect.getSequenceSql(sequenceName)
                        val statement = Statement(sql)
                        val executor = R2dbcExecutor(config, options)
                        val flow = executor.executeQuery(statement) { _, row -> row.get(0) }
                        when (val value = flow.firstOrNull() ?: error("No result: ${statement.toSql()}")) {
                            is Number -> value.toLong()
                            else -> error("The value class is not a Number. type=${value::class}")
                        }
                    }
                    idGenerator.property.setter(entity, id)
                } else null
            }
            else -> null
        }
        val clock = config.clockProvider.now()
        return context.target.preInsert(newEntity ?: entity, clock)
    }

    suspend fun <T> insert(config: R2dbcDatabaseConfig, execute: suspend (R2dbcExecutor) -> T): T {
        val generatedColumn = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.AutoIncrement<ENTITY, *> -> idGenerator.property.columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, options, generatedColumn)
        return execute(executor)
    }

    fun postInsert(entity: ENTITY, generatedKey: Long): ENTITY {
        val idGenerator = context.target.idGenerator()
        return if (idGenerator is IdGenerator.AutoIncrement<ENTITY, ID>) {
            val id = context.target.toId(generatedKey)!!
            idGenerator.property.setter(entity, id)
        } else {
            entity
        }
    }
}
