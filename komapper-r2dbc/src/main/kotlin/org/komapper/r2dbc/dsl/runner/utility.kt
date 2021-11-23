package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.options.QueryOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal suspend fun <ENTITY : Any, ID : Any> IdGenerator.Sequence<ENTITY, ID>.execute(
    config: R2dbcDatabaseConfig,
    options: QueryOptions,
): ID {
    return generate(config.id, config.dialect::enquote) { sequenceName ->
        val sql = config.dialect.getSequenceSql(sequenceName)
        val statement = Statement(sql)
        val executor = R2dbcExecutor(config, options)
        val flow = executor.executeQuery(statement) { _, row -> row.get(0) }
        when (val value = flow.firstOrNull() ?: error("No result: ${statement.toSql()}")) {
            is Number -> value.toLong()
            else -> error("The value class is not a Number. type=${value::class}")
        }
    }
}
