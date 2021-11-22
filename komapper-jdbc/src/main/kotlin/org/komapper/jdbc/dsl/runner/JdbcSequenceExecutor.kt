package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.runBlocking
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.options.QueryOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

class JdbcSequenceExecutor<ENTITY : Any, ID : Any>(
    private val config: JdbcDatabaseConfig,
    private val options: QueryOptions,
    private val idGenerator: IdGenerator.Sequence<ENTITY, ID>
) {
    fun execute(): ID = runBlocking {
        idGenerator.generate(config.id, config.dialect::enquote) { sequenceName ->
            val sql = config.dialect.getSequenceSql(sequenceName)
            val statement = Statement(sql)
            val executor = JdbcExecutor(config, options)
            executor.executeQuery(statement) { rs ->
                if (rs.next()) rs.getLong(1) else error("No result: ${statement.toSql()}")
            }
        }
    }
}
