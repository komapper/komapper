package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcEntityDeleteReturningRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityDeleteContext<ENTITY, ID, META>,
) {

    fun <T> delete(config: JdbcDatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = config.dialect.createExecutor(config, context.options)
        return execute(executor)
    }
}
