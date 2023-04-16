package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcEntityUpdateReturningRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
) {

    suspend fun <T> update(config: R2dbcDatabaseConfig, execute: suspend (R2dbcExecutor) -> T): T {
        val executor = R2dbcExecutor(config, context.options)
        return execute(executor)
    }
}
