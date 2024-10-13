package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcEntityUpdateRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
) {
    fun <T> update(config: JdbcDatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = config.dialect.createExecutor(config, context.options)
        return execute(executor)
    }
}
