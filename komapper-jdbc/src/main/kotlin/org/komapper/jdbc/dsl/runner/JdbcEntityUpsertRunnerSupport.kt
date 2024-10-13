package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcEntityUpsertRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
) {
    private val support: JdbcEntityInsertRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertRunnerSupport(context.insertContext)

    fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preInsert(config, entity)
    }

    fun <T> upsert(config: JdbcDatabaseConfig, usesGeneratedKeys: Boolean, execute: (JdbcExecutor) -> T): T {
        return support.insert(config, usesGeneratedKeys, execute)
    }
}
