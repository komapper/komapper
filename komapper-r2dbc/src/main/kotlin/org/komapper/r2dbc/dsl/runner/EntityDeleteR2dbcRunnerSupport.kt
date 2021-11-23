package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.VersionOptions
import org.komapper.core.dsl.runner.checkOptimisticLock
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class EntityDeleteR2dbcRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntityDeleteContext<ENTITY, ID, META>,
    val options: VersionOptions
) {

    suspend fun <T> delete(config: R2dbcDatabaseConfig, execute: suspend (R2dbcExecutor) -> T): T {
        val executor = R2dbcExecutor(config, options)
        return execute(executor)
    }

    fun postDelete(count: Int, index: Int? = null) {
        if (context.target.versionProperty() != null) {
            checkOptimisticLock(options, count, index)
        }
    }
}
