package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.runBlocking
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class EntityInsertJdbcRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) {

    fun preInsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        val newEntity = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.Sequence<ENTITY, ID> ->
                if (!context.target.disableSequenceAssignment() && !options.disableSequenceAssignment) {
                    runBlocking {
                        val id = idGenerator.generate(config.id, config.dialect::enquote) { sequenceName ->
                            val sql = config.dialect.getSequenceSql(sequenceName)
                            val statement = Statement(sql)
                            val executor = JdbcExecutor(config, options)
                            executor.executeQuery(statement) { rs ->
                                if (rs.next()) rs.getLong(1) else error("No result: ${statement.toSql()}")
                            }
                        }
                        idGenerator.property.setter(entity, id)
                    }
                } else null
            else -> null
        }
        val clock = config.clockProvider.now()
        return context.target.preInsert(newEntity ?: entity, clock)
    }

    fun <T> insert(config: JdbcDatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val requiresGeneratedKeys = context.target.idGenerator() is IdGenerator.AutoIncrement<ENTITY, *>
        val executor = JdbcExecutor(config, options, requiresGeneratedKeys)
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
