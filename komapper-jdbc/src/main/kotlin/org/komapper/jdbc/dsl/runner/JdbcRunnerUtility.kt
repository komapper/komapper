package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.runBlocking
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.options.QueryOptions
import org.komapper.jdbc.JdbcDatabaseConfig

internal fun <ENTITY : Any, ID : Any> IdGenerator.Sequence<ENTITY, ID>.execute(
    config: JdbcDatabaseConfig,
    options: QueryOptions,
): ID = runBlocking {
    generate(config.id, config.dialect::enquote) { sequenceName ->
        val sql = config.dialect.getSequenceSql(sequenceName)
        val statement = Statement(sql)
        val executor = config.dialect.createExecutor(config, options)
        executor.executeQuery(statement) { rs ->
            if (rs.next()) rs.getLong(1) else error("No result: ${statement.toSql()}")
        }
    }
}
