package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcEntityUpsertQueryRunnerSupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val options: InsertOptions
) {

    private val support: JdbcEntityInsertQueryRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertQueryRunnerSupport(context.insertContext, options)

    fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preInsert(config, entity)
    }

    fun <T> upsert(config: JdbcDatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, options)
        return execute(executor)
    }
}
