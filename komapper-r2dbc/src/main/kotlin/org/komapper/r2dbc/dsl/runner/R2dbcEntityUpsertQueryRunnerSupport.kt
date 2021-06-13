package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcEntityUpsertQueryRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) {

    private val support: R2dbcEntityInsertQueryRunnerSupport<ENTITY, ID, META> = R2dbcEntityInsertQueryRunnerSupport(context.insertContext, options)

    suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preInsert(config, entity)
    }

    suspend fun <T> upsert(config: R2dbcDatabaseConfig, execute: suspend (R2dbcExecutor) -> T): T {
        val executor = R2dbcExecutor(config, options)
        return execute(executor)
    }
}
