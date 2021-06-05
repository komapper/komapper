package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.InsertOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class EntityUpsertQuerySupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val option: InsertOption
) {

    val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context.insertContext, option)

    fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preInsert(config, entity)
    }

    suspend fun <T> upsert(config: R2dbcDatabaseConfig, execute: suspend (R2dbcExecutor) -> T): T {
        val executor = R2dbcExecutor(config, option)
        return execute(executor)
    }

    fun buildStatement(config: R2dbcDatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entities)
        return builder.build()
    }
}
