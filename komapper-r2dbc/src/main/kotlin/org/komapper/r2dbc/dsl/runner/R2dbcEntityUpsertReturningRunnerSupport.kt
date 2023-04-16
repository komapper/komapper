package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcEntityUpsertReturningRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
) {

    private val support: R2dbcEntityInsertRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityInsertRunnerSupport(context.insertContext)

    suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preInsert(config, entity)
    }

    suspend fun <T> upsert(config: R2dbcDatabaseConfig, usesGeneratedKeys: Boolean, execute: suspend (R2dbcExecutor) -> T): T {
        return support.insert(config, usesGeneratedKeys, execute)
    }
}
