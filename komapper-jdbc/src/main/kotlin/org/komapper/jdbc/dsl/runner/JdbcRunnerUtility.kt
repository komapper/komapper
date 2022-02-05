package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.runBlocking
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.options.QueryOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal fun <ENTITY : Any, ID : Any> IdGenerator.Sequence<ENTITY, ID>.execute(
    config: JdbcDatabaseConfig,
    options: QueryOptions,
): ID = runBlocking {
    generate(config.id, config.dialect::enquote) { sequenceName ->
        val sql = config.dialect.getSequenceSql(sequenceName)
        val statement = Statement(sql)
        val executor = JdbcExecutor(config, options)
        executor.executeQuery(statement) { rs ->
            if (rs.next()) rs.getLong(1) else error("No result: ${statement.toSql()}")
        }
    }
}

internal fun customizeBatchCounts(counts: IntArray): IntArray {
    val results = IntArray(counts.size)
    for ((index, count) in counts.withIndex()) {
        results[index] = when (count) {
            java.sql.Statement.EXECUTE_FAILED -> 0
            java.sql.Statement.SUCCESS_NO_INFO -> 1
            else -> count
        }
    }
    return results
}
