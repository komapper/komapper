package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.SqlExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.QueryOption

internal class EntityUpsertQuerySupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val option: QueryOption
) {

    val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context.insertContext, option)

    fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preInsert(config, entity)
    }

    fun <T> upsert(config: DatabaseConfig, execute: (SqlExecutor) -> T): T {
        val executor = SqlExecutor(config, option)
        return execute(executor)
    }

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entities)
        return builder.build()
    }
}
